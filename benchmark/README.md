# Cesi EMS Performance Benchmark

Performance benchmark suite for paper **§5.4 Performance Evaluation**.

## Prerequisites

1. **JMeter 5.6+** — Download from https://jmeter.apache.org/download_jmeter.cgi
2. **Java 8+** — Required by JMeter
3. **Backend running** on `http://127.0.0.1:8080`
4. **InfluxDB running** on `http://127.0.0.1:8086`
5. **Seed data loaded** — `cesi-api/sql/public-v3.sql` executed

## Test Plan Overview

| Test | Purpose | Threads | Duration | Paper Section |
|------|---------|---------|----------|---------------|
| Test 1 | API response time baseline | 1 | ~2 min | Table 3 — Query Latency |
| Test 2 | Concurrent user stress test | 100 (ramp 120s) | 5 min | Table 3 — Concurrent Users |
| Test 3 | InfluxDB write throughput | 10 | 2 min | Table 3 — Write Throughput |
| Test 4 | InfluxDB query latency | 5 | ~2 min | Table 3 — TSDB Query |

### Test 1: API Response Time Baseline
Single-user, 100 iterations across 6 transaction groups:
- **TC-01** Dashboard Load (4 homepage APIs)
- **TC-02** Energy Analysis (4 consumption analysis APIs)
- **TC-03** Carbon Emission (2 carbon APIs)
- **TC-04** Product Carbon Footprint (3 PCF APIs)
- **TC-05** Budget Analysis (1 budget API)
- **TC-06** Real-time Monitor (1 TSDB query API)

### Test 2: Concurrent Users
100 threads ramp up over 120s, run for 5 minutes total. Each thread randomly picks from 6 API endpoints simulating realistic user browsing with Gaussian think time (1000±500ms).

### Test 3: InfluxDB Write Throughput
10 concurrent threads, each writing batches of 50 meter data points (line protocol). Simulates 50 smart meters reporting voltage, current, power, energy, and power factor. Also includes a 500-point batch test using Groovy script.

### Test 4: InfluxDB Query Latency
5 concurrent threads, 100 iterations of 3 Flux query types:
- **Snapshot**: Last 1h, all meters, `last()` aggregation
- **Daily Aggregation**: 30-day window, `sum()` by day
- **Workshop Comparison**: 7-day window, `mean()` by workshop per hour

## Quick Start

```powershell
# Run all tests
.\run-benchmark.ps1 -JMeterHome "C:\apache-jmeter-5.6.3"

# Run only InfluxDB write test
.\run-benchmark.ps1 -JMeterHome "C:\apache-jmeter-5.6.3" -TestOnly 3

# Analyze results
.\analyze-results.ps1

# Or run JMeter directly (non-GUI mode)
C:\apache-jmeter-5.6.3\bin\jmeter.bat -n -t jmeter\cesi-ems-benchmark.jmx -l results\output.csv -e -o results\report

# Open JMeter GUI to inspect/modify test plan
C:\apache-jmeter-5.6.3\bin\jmeter.bat -t jmeter\cesi-ems-benchmark.jmx
```

## Configuration

Edit variables in the `.jmx` file or override via JMeter properties:

| Variable | Default | Description |
|----------|---------|-------------|
| `BASE_URL` | `127.0.0.1` | Backend host |
| `PORT` | `8080` | Backend port |
| `USERNAME` | `admin` | Login username |
| `PASSWORD` | `123456` | Login password |
| `INFLUXDB_URL` | `127.0.0.1` | InfluxDB host |
| `INFLUXDB_PORT` | `8086` | InfluxDB port |
| `INFLUXDB_TOKEN` | `CHANGE_ME` | InfluxDB auth token |
| `INFLUXDB_ORG` | `org` | InfluxDB organization |
| `INFLUXDB_BUCKET` | `bucket` | InfluxDB bucket |

Override example:
```powershell
jmeter -n -t jmeter\cesi-ems-benchmark.jmx -JBASE_URL=192.168.1.100 -JPORT=8080
```

## Output

- `results/test*_<timestamp>.csv` — Raw JMeter results
- `results/test*_report_<timestamp>/index.html` — JMeter HTML dashboard
- Console output from `analyze-results.ps1` — Paper-ready summary table

## Paper Table Template (§5.4)

After running all tests, `analyze-results.ps1` generates **Table 3** for the paper:

```
Table 3: System Performance Evaluation Results

| Metric                              | Value         |
|-------------------------------------|---------------|
| Dashboard load time (avg)           | XX ms         |
| Dashboard load time (P95)           | XX ms         |
| Energy analysis query (avg)         | XX ms         |
| Carbon emission query (avg)         | XX ms         |
| PCF lifecycle query (avg)           | XX ms         |
| Concurrent users supported          | 100           |
| Throughput (100 users)              | XX req/s      |
| Success rate (100 users)            | XX.XX%        |
| P95 response time (100 users)       | XX ms         |
| InfluxDB write throughput           | XX,XXX pts/s  |
| InfluxDB write latency (avg)        | XX ms         |
| InfluxDB snapshot query (avg)       | XX ms         |
| InfluxDB aggregation query (P95)    | XX ms         |
| InfluxDB comparison query (P95)     | XX ms         |
```
