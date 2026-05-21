Deployment and Monitoring Guide

Overview

- CI builds, tests, produces Docker image and pushes to a container registry (e.g. GitHub Container Registry).
- CD deploys that image to Koyeb (or other target) and performs health checks.
- Monitoring uses Spring Boot Actuator + Micrometer (Prometheus) and a Grafana dashboard.

Required repository secrets / variables

- `KOYEB_API_TOKEN` (secret) — Koyeb personal API token
- `GITHUB_TOKEN` (automatically provided) or `CR_PAT` (for GHCR push, if needed)
- Repository `vars` expected by workflows: `KOYEB_APP_NAME`, `KOYEB_SERVICE_NAME`

CI/CD workflow (high level)

1. Run `./gradlew clean check` (includes tests, checkstyle, Jacoco verification).
2. Build Docker image and push to registry (GHCR or Docker Hub).
3. Trigger deployment on Koyeb using image reference and ensure health endpoint passes.

Local run

- Build:

```bash
./gradlew clean bootJar
```

- Run with custom port:

```bash
PORT=8080 java -jar build/libs/app.jar
```

Monitoring

- Actuator exposes Prometheus metrics at `/actuator/prometheus` and health at `/actuator/health`.
- Example Prometheus job in `monitoring/prometheus-scrape.yml`.
- A sample Grafana dashboard is in `monitoring/grafana/dashboard.json`.

Notes & Next steps

- Pin external GitHub Actions to full commit SHAs in workflows to satisfy supply-chain checks.
- Provide `KOYEB_API_TOKEN` secret and repository variables to enable automated CD.
- Consider adding Terraform (IaC) for Koyeb resources — a sample `infra/` README is included.
