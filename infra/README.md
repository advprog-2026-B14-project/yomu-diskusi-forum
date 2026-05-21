Infrastructure (sample guidance)

This folder contains guidance for creating infrastructure resources for production deployments.

Koyeb

- Koyeb does not require a VM; it runs services from container images.
- Recommended IaC flow:
  - Build and push image to a registry (GHCR).
  - Use Terraform with the `koyeb` provider to create apps/services and link to the pushed image.

Sample Terraform (not included):

- Create `main.tf` with provider configuration using `KOYEB_API_TOKEN` as an environment variable.
- Define `koyeb_app` and `koyeb_service` resources, pointing the service to the container image.

Security

- Store API tokens in repo secrets.
- Pin GitHub Actions to commit SHAs.

Operational notes

- Ensure the app exposes `/actuator/health` for health checks and `/actuator/prometheus` for metrics.
- Add alerting rules in Prometheus for service-down and high error rates.
