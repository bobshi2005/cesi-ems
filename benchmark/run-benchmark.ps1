<#
.SYNOPSIS
    Cesi EMS Performance Benchmark Runner
.DESCRIPTION
    Runs JMeter test plans for paper §5.4 Performance Evaluation.
    Prerequisites: JMeter 5.6+, Java 8+, backend running on :8080, InfluxDB on :8086
.PARAMETER JMeterHome
    Path to JMeter installation directory (e.g., C:\apache-jmeter-5.6.3)
.PARAMETER TestOnly
    Run only specific test(s): 1=API Baseline, 2=Concurrent, 3=InfluxDB Write, 4=InfluxDB Query
.EXAMPLE
    .\run-benchmark.ps1 -JMeterHome "C:\apache-jmeter-5.6.3"
    .\run-benchmark.ps1 -JMeterHome "C:\apache-jmeter-5.6.3" -TestOnly 3
#>
param(
    [Parameter(Mandatory=$true)]
    [string]$JMeterHome,
    [int]$TestOnly = 0
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$JMX = Join-Path $ScriptDir "jmeter\cesi-ems-benchmark.jmx"
$ResultsDir = Join-Path $ScriptDir "results"
$JMeterBin = Join-Path $JMeterHome "bin\jmeter.bat"

if (-not (Test-Path $JMeterBin)) {
    Write-Error "JMeter not found at $JMeterBin. Please provide correct -JMeterHome path."
    exit 1
}

New-Item -ItemType Directory -Force $ResultsDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Cesi EMS Performance Benchmark" -ForegroundColor Cyan
Write-Host " Timestamp: $timestamp" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# --- Pre-flight checks ---
Write-Host "`n[Pre-flight] Checking backend..." -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "http://127.0.0.1:8080/captchaImage" -Method GET -TimeoutSec 5
    Write-Host "  Backend is running." -ForegroundColor Green
} catch {
    Write-Host "  WARNING: Backend not reachable at http://127.0.0.1:8080" -ForegroundColor Red
    Write-Host "  Start the backend first: mvn spring-boot:run -pl cesi-admin" -ForegroundColor Red
}

Write-Host "[Pre-flight] Checking InfluxDB..." -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "http://127.0.0.1:8086/health" -Method GET -TimeoutSec 5
    Write-Host "  InfluxDB is running. Status: $($resp.status)" -ForegroundColor Green
} catch {
    Write-Host "  WARNING: InfluxDB not reachable at http://127.0.0.1:8086" -ForegroundColor Red
}

# --- Define tests ---
$tests = @(
    @{ Id=1; Name="Test 1 - API Response Time Baseline"; ThreadGroup="Test 1 - API Response Time Baseline"; Duration="~2 min" }
    @{ Id=2; Name="Test 2 - Concurrent Users (100)"; ThreadGroup="Test 2 - Concurrent Users (Ramp 10→50→100)"; Duration="~5 min" }
    @{ Id=3; Name="Test 3 - InfluxDB Write Throughput"; ThreadGroup="Test 3 - InfluxDB Write Throughput"; Duration="~2 min" }
    @{ Id=4; Name="Test 4 - InfluxDB Query Latency"; ThreadGroup="Test 4 - InfluxDB Query Latency"; Duration="~2 min" }
)

foreach ($test in $tests) {
    if ($TestOnly -ne 0 -and $test.Id -ne $TestOnly) { continue }

    $resultFile = Join-Path $ResultsDir "test$($test.Id)_${timestamp}.csv"
    $logFile = Join-Path $ResultsDir "test$($test.Id)_${timestamp}.log"
    $reportDir = Join-Path $ResultsDir "test$($test.Id)_report_${timestamp}"

    Write-Host "`n--- Running: $($test.Name) (Est. $($test.Duration)) ---" -ForegroundColor Yellow

    & $JMeterBin -n -t $JMX -l $resultFile -j $logFile -e -o $reportDir `
        -JThreadGroup="$($test.ThreadGroup)"

    if ($LASTEXITCODE -eq 0) {
        Write-Host "  PASSED. Results: $resultFile" -ForegroundColor Green
        Write-Host "  HTML Report: $reportDir\index.html" -ForegroundColor Green
    } else {
        Write-Host "  FAILED. Check log: $logFile" -ForegroundColor Red
    }
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " Benchmark Complete" -ForegroundColor Cyan
Write-Host " Results directory: $ResultsDir" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "`nNext step: Run analyze-results.ps1 to generate paper-ready tables."
