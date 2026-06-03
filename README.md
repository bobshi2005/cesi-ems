# CESI EMS — Integrated Carbon-Energy Management System

Reference implementation of CESI EMS, an open-architecture web-based platform
integrating real-time energy monitoring, multi-granularity consumption
analysis, scope-aware carbon accounting (ISO 14064-1 / MEE), five-stage
product carbon footprint assessment (ISO 14067), supply-chain Scope 3
tracking, and a four-phase digitized MRV workflow on a single technology
stack (MQTT, InfluxDB 2.7, PostgreSQL 14, Spring Boot 4.0.6, Vue 3).

This codebase accompanies the manuscript
*"CESI EMS: An Integrated Carbon-Energy Management System with IoT-to-Carbon
Data Pipeline, Product Carbon Footprint, and Digitized MRV"* submitted to
*Journal of Cleaner Production*.

## Repository layout

```
cesi-ems/
├── cesi-api/         # Java / Spring Boot backend (Maven multi-module)
├── cesi-web/         # Vue 3 + Vite frontend SPA
├── benchmark/        # JMeter 5.6.3 test plan + reported results
├── .env.example      # Environment-variable template (copy to .env.file)
├── CITATION.cff
├── LICENSE
└── README.md
```

## Prerequisites

| Service     | Version              | Default Port | Purpose                                          |
|-------------|----------------------|--------------|--------------------------------------------------|
| Java        | 21+                  | -            | Backend build / runtime                          |
| Maven       | 3.6+                 | -            | Backend build                                    |
| Node.js     | 18+                  | -            | Frontend build                                   |
| Yarn        | 1.x                  | -            | Frontend package manager (do not mix with npm)   |
| PostgreSQL  | 14                   | 5432         | Primary relational DB (`cesi_ems`)               |
| Redis       | 6+                   | 6379         | Cache and session store                          |
| InfluxDB    | 2.7+                 | 8086         | Time-series storage for real-time meter readings |
| MQTT broker | any                  | 1883         | (Optional) IoT device data ingestion             |

## Quick start

### 1. Configure environment

```bash
cp .env.example .env.file
# Edit .env.file: at minimum set DB_PASSWORD, REDIS_PASSWORD,
# RTDB_TOKEN, TOKEN_SECRET (any random 64+ character string).
```

### 2. Initialize database

```bash
psql -U postgres -c "CREATE DATABASE cesi_ems;"
psql -U postgres -d cesi_ems -f cesi-api/sql/public.sql
```

The SQL seed in this repository is sanitized: real user records, contact
information, internal LAN addresses, and external production URLs have been
replaced with placeholders. The schema, demonstration topology, and
12-month simulated meter dataset reported in §5.3 of the paper are
preserved.

### 3. Start backend

```bash
cd cesi-api
mvn clean package -Dmaven.test.skip=true
mvn spring-boot:run -pl cesi-admin
# Listens on http://localhost:8080
```

### 4. Start frontend (dev)

```bash
cd cesi-web
yarn install
yarn dev
# Listens on http://localhost:80 with /dev-api -> http://127.0.0.1:8080
```

Default credentials: `admin` / `123456`

## Reproducing the benchmark reported in the paper

The full JMeter performance benchmark reported in §5.4 / Table 5 is at:

- `benchmark/jmeter/cesi-ems-benchmark.jmx` — JMeter 5.6.3 test plan
- `benchmark/results/full_20260531_130053.csv` — raw sample log (JMeter `.jtl` CSV format)
- `benchmark/results/full_20260531_130053.log` / `*_stdout.log` — run logs
- `benchmark/results/full_report_20260531_130053/` — HTML dashboard
- `benchmark/README.md` — measurement protocol

To re-run on your own deployment:

```powershell
cd benchmark
./run-benchmark.ps1
```

## Citation

If you use this software in academic work, please cite the accompanying
paper. See `CITATION.cff` for machine-readable citation metadata.

## License

Licensed under the MIT License — see `LICENSE` for details.