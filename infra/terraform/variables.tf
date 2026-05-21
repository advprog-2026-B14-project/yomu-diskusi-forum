variable "koyeb_api_token" {
  type        = string
  description = "Koyeb API token (supply via environment or CI secret)"
  sensitive   = true
}

variable "app_name" {
  type    = string
  default = "yomu-forum-app"
}

variable "service_name" {
  type    = string
  default = "yomu-forum-service"
}

variable "image" {
  type        = string
  description = "Container image (e.g. ghcr.io/org/yomu-forum:sha)"
}
