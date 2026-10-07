# 1. Habilitación de APIs necesarias
locals {
  services = [
    "run.googleapis.com",
    "sqladmin.googleapis.com",
    "apigateway.googleapis.com",
    "servicecontrol.googleapis.com",
    "servicemanagement.googleapis.com",
    "artifactregistry.googleapis.com",
    "cloudbuild.googleapis.com",
    "secretmanager.googleapis.com",
    "logging.googleapis.com",
    "iam.googleapis.com"
  ]
}

resource "google_project_service" "apis" {
  for_each                   = toset(local.services)
  project                    = var.project_id
  service                    = each.value
  disable_dependent_services = false
  disable_on_destroy         = false
}

# 2. Artifact Registry
resource "google_artifact_registry_repository" "repo" {
  depends_on    = [google_project_service.apis]
  location      = var.region
  repository_id = "${var.app_name}-repo"
  description   = "Repositorio Docker para imágenes del backend"
  format        = "DOCKER"
}

# 3. Secret Manager
resource "google_secret_manager_secret" "jwt_secret" {
  depends_on = [google_project_service.apis]
  secret_id  = "JWT_SECRET"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "jwt_secret_val" {
  secret      = google_secret_manager_secret.jwt_secret.id
  secret_data = var.jwt_secret
}

resource "google_secret_manager_secret" "db_url" {
  depends_on = [google_project_service.apis]
  secret_id  = "DATABASE_URL"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "db_url_val" {
  secret      = google_secret_manager_secret.db_url.id
  secret_data = "postgresql+psycopg2://${google_sql_user.db_user.name}:${var.db_password}@/${google_sql_database.db.name}?host=/cloudsql/${google_sql_database_instance.postgres.connection_name}"
}

# 4. Cloud SQL (PostgreSQL Serverless Tier)
resource "random_id" "db_suffix" {
  byte_length = 4
}

resource "google_sql_database_instance" "postgres" {
  depends_on          = [google_project_service.apis]
  name                = "${var.app_name}-db-${random_id.db_suffix.hex}"
  database_version    = "POSTGRES_15"
  region              = var.region
  deletion_protection = false

  settings {
    tier = "db-f1-micro"
    ip_configuration {
      ipv4_enabled = true
    }
  }
}

resource "google_sql_database" "db" {
  name     = "app_database"
  instance = google_sql_database_instance.postgres.name
}

resource "google_sql_user" "db_user" {
  name     = "app_user"
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
}

# 5. Cuentas de Servicio e IAM
resource "google_service_account" "cloudrun_sa" {
  depends_on   = [google_project_service.apis]
  account_id   = "${var.app_name}-cr-sa"
  display_name = "Cloud Run Runtime SA"
}

resource "google_project_iam_member" "cloudsql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloudrun_sa.email}"
}

resource "google_project_iam_member" "secret_accessor" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.cloudrun_sa.email}"
}

resource "google_project_iam_member" "cloud_logging" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.cloudrun_sa.email}"
}

# 6. Cloud Run
resource "google_cloud_run_v2_service" "backend" {
  depends_on = [
    google_project_service.apis,
    google_sql_database_instance.postgres,
    google_secret_manager_secret_version.jwt_secret_val,
    google_secret_manager_secret_version.db_url_val
    google_project_iam_member.secret_accessor
  ]
  name     = "${var.app_name}-backend"
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.cloudrun_sa.email

    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [google_sql_database_instance.postgres.connection_name]
      }
    }

    containers {
      image = var.container_image

      resources {
        limits = {
          cpu    = "1000m"
          memory = "512Mi"
        }
      }

      env {
        name = "JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.jwt_secret.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "DATABASE_URL"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_url.secret_id
            version = "latest"
          }
        }
      }

      volume_mounts {
        name       = "cloudsql"
        mount_path = "/cloudsql"
      }
    }
  }
}

# Permitir acceso público / Gateway a Cloud Run
resource "google_cloud_run_v2_service_iam_member" "cr_invoker" {
  project  = var.project_id
  location = google_cloud_run_v2_service.backend.location
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# 7. API Gateway
resource "google_service_account" "gateway_sa" {
  depends_on   = [google_project_service.apis]
  account_id   = "${var.app_name}-gw-sa"
  display_name = "API Gateway SA"
}

resource "google_project_iam_member" "gw_invoker_role" {
  project = var.project_id
  role    = "roles/run.invoker"
  member  = "serviceAccount:${google_service_account.gateway_sa.email}"
}

resource "google_api_gateway_api" "api" {
  provider     = google-beta
  depends_on   = [google_project_service.apis]
  api_id       = "${var.app_name}-api"
  display_name = "API Gateway para ${var.app_name}"
}

resource "google_api_gateway_api_config" "api_cfg" {
  provider      = google-beta
  api           = google_api_gateway_api.api.api_id
  api_config_id = "v1-${random_id.db_suffix.hex}"

  openapi_documents {
    document {
      path = "spec.yaml"
      contents = base64encode(templatefile("${path.module}/openapi2.yaml", {
        backend_url = google_cloud_run_v2_service.backend.uri
      }))
    }
  }

  gateway_config {
    backend_config {
      google_service_account = google_service_account.gateway_sa.email
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "google_api_gateway_gateway" "gw" {
  provider   = google-beta
  api_config = google_api_gateway_api_config.api_cfg.id
  gateway_id = "${var.app_name}-gateway"
  region     = var.region
}