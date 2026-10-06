output "cloud_run_url" {
  value       = google_cloud_run_v2_service.backend.uri
  description = "URL directa del servicio Cloud Run"
}

output "api_gateway_url" {
  value       = "https://${google_api_gateway_gateway.gw.default_hostname}"
  description = "URL base del API Gateway"
}

output "artifact_registry_repo" {
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.repo.name}"
  description = "URL base de Artifact Registry"
}

output "cloud_sql_connection_name" {
  value       = google_sql_database_instance.postgres.connection_name
  description = "Connection Name para Cloud SQL Proxy"
}