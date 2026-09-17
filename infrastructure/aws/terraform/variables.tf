# ==============================================================================
# FASE 13: TERRAFORM VARIABLES - PLATAFORMA CASE EN AWS
# ==============================================================================

variable "aws_region" {
  description = "Región de AWS donde se desplegarán todos los recursos"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente de despliegue (production, staging, development)"
  type        = string
  default     = "production"
}

variable "project_name" {
  description = "Nombre identificador del proyecto"
  type        = string
  default     = "case-platform"
}

variable "vpc_cidr" {
  description = "Rango CIDR para la VPC privada de producción"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_instance_class" {
  description = "Tipo de instancia para Amazon RDS PostgreSQL"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_name" {
  description = "Nombre de la base de datos PostgreSQL en RDS"
  type        = string
  default     = "case_platform_db"
}

variable "db_username" {
  description = "Usuario administrador de RDS PostgreSQL"
  type        = string
  default     = "case_admin"
}

variable "db_password" {
  description = "Contraseña de producción para RDS PostgreSQL"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "Clave secreta para firma y verificación de tokens JWT"
  type        = string
  sensitive   = true
}

variable "backend_cpu" {
  description = "Unidades de CPU para la tarea Fargate del backend (256, 512, 1024)"
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "Memoria RAM en MB para la tarea Fargate del backend (512, 1024, 2048)"
  type        = number
  default     = 1024
}
