variable "project_id" {
  type        = string
  description = "ID del proyecto en Google Cloud Platform"
}

variable "region" {
  type        = string
  description = "Región de GCP para el despliegue de recursos"
  default     = "us-central1"
}

variable "app_name" {
  type        = string
  description = "Nombre base de la aplicación"
  default     = "app-serverless"
}

variable "db_password" {
  type        = string
  description = "Contraseña del usuario de Cloud SQL PostgreSQL"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "Clave secreta para la firma de tokens JWT"
  sensitive   = true
}

variable "container_image" {
  type        = string
  description = "URL de la imagen inicial en Artifact Registry"
  default     = "us-docker.pkg.dev/cloudrun/container/hello"
}