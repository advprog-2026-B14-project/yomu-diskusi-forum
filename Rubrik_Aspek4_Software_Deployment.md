Rubrik Penilaian Akhir Kelompok — Aspek 4: Software Deployment

Tujuan

- Menilai kesiapan deployment end-to-end: CI/CD, containerization, provisioning/IaC, strategi deploy, rollback, dan observability produksi.

Skala dan Kriteria

Skala 0 — Local only

- Aplikasi hanya dapat dijalankan di localhost tanpa pipeline otomatis.
- Belum ada containerization, monitoring, atau artefak deploy yang bisa dieksekusi ulang.

Skala 1 — Manual infra, mulai observability

- Ada environment deployment, tetapi provisioning dan deploy masih manual atau semi-manual.
- Monitoring mulai disiapkan, namun belum jelas ada scrape, dashboard, atau alert yang aktif.

Skala 2 — CI/CD + IaC + Monitoring dasar

- Ada CI yang otomatis menjalankan build, test, dan quality gates.
- Ada workflow CD yang membangun image dan melakukan deploy otomatis.
- Ada IaC atau referensi provisioning yang bisa dipakai ulang.
- Metrics diekspos oleh aplikasi dan dapat di-scrape oleh Prometheus.
- Grafana dashboard dan alert rule tersedia sebagai bukti observability dasar.

Skala 3 — Strategy + Relevance

- Ada strategi deployment yang jelas, misalnya rolling atau canary, dan alasannya didokumentasikan.
- Metrics yang dipantau relevan terhadap kesehatan sistem dan KPI aplikasi: latency, error rate, throughput, JVM memory, dan GC.
- Deployment manifest menunjukkan probe, replica, dan resource policy yang mendukung zero-downtime rollout.

Skala 4 — Advanced procedures

- Rollback otomatis tersedia dan bisa dipicu saat rollout gagal.
- Load balancing dan autoscaling dikonfigurasi atau didukung oleh manifest/infrastruktur.
- Ada bukti prosedur recovery atau rollback yang dapat diuji ulang, bukan sekadar disebutkan.

Checklist Bukti Artefak (mapping ke repo)

- CI: `.github/workflows/ci.yml` (build, test, Checkstyle, JaCoCo, dependency check)
- CD: `.github/workflows/cd.yml` (build, test, image push, deploy)
- Canary: `.github/workflows/canary-deploy.yml`
- Rollback: `.github/workflows/rollback.yml`
- Security/quality tambahan: `.github/workflows/sonarqube.yml`, `.github/workflows/codeql.yml`, `.github/workflows/scorecard.yml`
- Container: `Dockerfile` multi-stage dengan healthcheck
- Kubernetes: `k8s/deployment.yaml`, `k8s/service.yaml`, `k8s/ingress.yaml`
- IaC / referensi deploy: `infra/terraform/main.tf`, `infra/terraform/variables.tf`, `infra/DEPLOYMENT_REFERENCE.md`, `infra/README.md`
- Monitoring: `src/main/resources/application.properties`, `monitoring/prometheus-scrape.yml`, `monitoring/alerts.yml`, `monitoring/grafana/dashboard.json`
- Security hardening: action pinning di workflow, permission minimal, dan health/readiness probe pada deployment

Panduan Penilaian Singkat untuk Dosen

- 0: Hanya ada instruksi run lokal atau dokumentasi dasar.
- 1: Ada environment deployment atau dokumentasi infra, tetapi belum ada otomasi yang solid.
- 2: Ada CI/CD, artefak container, metrics, dan dashboard/alert dasar.
- 3: Ada strategi deploy yang jelas, manifest produksi yang layak, dan metrics yang relevan.
- 4: Ada rollback otomatis, prosedur recovery, dan konfigurasi operasional yang siap diuji.

Catatan untuk repo ini

- Repo ini sudah menunjukkan level 3 ke atas untuk sebagian besar indikator, karena memiliki CI/CD, canary, rollback, manifest Kubernetes, healthcheck container, dan monitoring Prometheus/Grafana.
- Jika ingin menaikkan bukti ke level 4, yang paling kuat adalah menambah validasi autoscaling, skenario rollback teruji, dan dokumentasi recovery yang eksplisit.
