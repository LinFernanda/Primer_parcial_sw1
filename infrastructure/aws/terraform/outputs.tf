# ==============================================================================
# FASE 13: OUTPUTS DE TERRAFORM
# Endpoints y referencias clave del despliegue en AWS
# ==============================================================================

output "alb_dns_name" {
  description = "Nombre DNS público del Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "Endpoint de conexión para PostgreSQL en Amazon RDS"
  value       = aws_db_instance.postgres.endpoint
}

output "s3_bucket_name" {
  description = "Nombre del bucket S3 para almacenamiento de artefactos y diagramas"
  value       = aws_s3_bucket.assets.id
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS Fargate"
  value       = aws_ecs_cluster.main.name
}

output "cloudwatch_log_group" {
  description = "Grupo de logs en CloudWatch para el backend"
  value       = aws_cloudwatch_log_group.backend_logs.name
}
