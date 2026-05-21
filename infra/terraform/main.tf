// Sample Terraform for Koyeb app and service (example only - adapt to your org)
terraform {
  required_providers {
    koyeb = {
      source  = "koyeb/koyeb"
      version = "~> 0.2"
    }
  }
}

provider "koyeb" {
  api_token = var.koyeb_api_token
}

resource "koyeb_app" "yomu" {
  name = var.app_name
}

resource "koyeb_service" "yomu_service" {
  app_id = koyeb_app.yomu.id
  name   = var.service_name

  docker_image {
    image = var.image
  }

  port {
    internal = 8080
    protocol = "TCP"
  }
}

output "service_id" {
  value = koyeb_service.yomu_service.id
}
