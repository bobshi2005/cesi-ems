<#
.SYNOPSIS
    Analyze JMeter benchmark results and generate paper-ready tables for §5.4.
.PARAMETER ResultsDir
    Path to the results directory containing CSV files from JMeter runs.
.EXAMPLE
    .\analyze-results.ps1 -ResultsDir ".\results"
#>
param(
    [string]$ResultsDir = (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "results")
)

function Get-Percentile {
    param([double[]]$Values, [double]$Percentile)
    if ($Values.Count -eq 0) { return 0 }
    $sorted = $Values | Sort-Object
    $idx = [math]::Ceiling($Percentile / 100.0 * $sorted.Count) - 1
    if ($idx -lt 0) { $idx = 0 }
    return $sorted[$idx]
}

function Analyze-CsvFile {
    param([string]$FilePath, [string]$TestName)

    if (-not (Test-Path $FilePath)) {
        Write-Host "  File not found: $FilePath" -ForegroundColor Red
        return
    }

    $data = Import-Csv $FilePath
    if ($data.Count -eq 0) {
        Write-Host "  No data in: $FilePath" -ForegroundColor Red
        return
    }

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host " $TestName" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    $groups = $data | Group-Object -Property label

    Write-Host "`n{0,-50} {1,8} {2,8} {3,8} {4,8} {5,8} {6,8} {7,6}" -f "Sampler", "Count", "Avg(ms)", "P50(ms)", "P95(ms)", "P99(ms)", "Max(ms)", "Err%"
    Write-Host ("{0}" -f ("-" * 106))

    foreach ($group in $groups) {
        $times = $group.Group | ForEach-Object { [double]$_.elapsed }
        $errors = ($group.Group | Where-Object { $_.success -eq "false" }).Count
        $errPct = if ($group.Count -gt 0) { [math]::Round($errors / $group.Count * 100, 2) } else { 0 }

        $avg = [math]::Round(($times | Measure-Object -Average).Average, 1)
        $p50 = [math]::Round((Get-Percentile $times 50), 1)
        $p95 = [math]::Round((Get-Percentile $times 95), 1)
        $p99 = [math]::Round((Get-Percentile $times 99), 1)
        $max = [math]::Round(($times | Measure-Object -Maximum).Maximum, 1)

        Write-Host ("{0,-50} {1,8} {2,8} {3,8} {4,8} {5,8} {6,8} {7,5}%" -f
            ($group.Name.Substring(0, [math]::Min(50, $group.Name.Length))),
            $group.Count, $avg, $p50, $p95, $p99, $max, $errPct)
    }

    # Overall summary
    $allTimes = $data | ForEach-Object { [double]$_.elapsed }
    $allErrors = ($data | Where-Object { $_.success -eq "false" }).Count
    $totalErrPct = if ($data.Count -gt 0) { [math]::Round($allErrors / $data.Count * 100, 2) } else { 0 }
    $overallAvg = [math]::Round(($allTimes | Measure-Object -Average).Average, 1)
    $overallP50 = [math]::Round((Get-Percentile $allTimes 50), 1)
    $overallP95 = [math]::Round((Get-Percentile $allTimes 95), 1)
    $overallP99 = [math]::Round((Get-Percentile $allTimes 99), 1)
    $overallMax = [math]::Round(($allTimes | Measure-Object -Maximum).Maximum, 1)

    # Calculate throughput
    $timestamps = $data | ForEach-Object { [long]$_.timeStamp }
    $minTs = ($timestamps | Measure-Object -Minimum).Minimum
    $maxTs = ($timestamps | Measure-Object -Maximum).Maximum
    $durationSec = ($maxTs - $minTs) / 1000.0
    $throughput = if ($durationSec -gt 0) { [math]::Round($data.Count / $durationSec, 1) } else { 0 }

    Write-Host ("{0}" -f ("-" * 106))
    Write-Host ("{0,-50} {1,8} {2,8} {3,8} {4,8} {5,8} {6,8} {7,5}%" -f
        "OVERALL", $data.Count, $overallAvg, $overallP50, $overallP95, $overallP99, $overallMax, $totalErrPct)
    Write-Host "`nThroughput: $throughput requests/sec | Duration: $([math]::Round($durationSec, 1))s | Total samples: $($data.Count)"
}

function Generate-PaperTable {
    param([string]$ResultsDir)

    Write-Host "`n`n" -NoNewline
    Write-Host "================================================================" -ForegroundColor Green
    Write-Host " PAPER-READY TABLE (for §5.4 Performance Evaluation)" -ForegroundColor Green
    Write-Host "================================================================" -ForegroundColor Green

    Write-Host @"

Table 3: System Performance Evaluation Results
+---------------------------------------------+------------------+
| Test Scenario                               | Result           |
+---------------------------------------------+------------------+
"@ -ForegroundColor White

    # Try to extract key metrics from the CSV files
    $csvFiles = Get-ChildItem $ResultsDir -Filter "*.csv" | Sort-Object Name

    foreach ($csv in $csvFiles) {
        $data = Import-Csv $csv.FullName
        if ($data.Count -eq 0) { continue }

        $allTimes = $data | ForEach-Object { [double]$_.elapsed }
        $avg = [math]::Round(($allTimes | Measure-Object -Average).Average, 1)
        $p95 = [math]::Round((Get-Percentile $allTimes 95), 1)

        $timestamps = $data | ForEach-Object { [long]$_.timeStamp }
        $minTs = ($timestamps | Measure-Object -Minimum).Minimum
        $maxTs = ($timestamps | Measure-Object -Maximum).Maximum
        $durationSec = ($maxTs - $minTs) / 1000.0
        $throughput = if ($durationSec -gt 0) { [math]::Round($data.Count / $durationSec, 1) } else { 0 }

        $errors = ($data | Where-Object { $_.success -eq "false" }).Count
        $successRate = if ($data.Count -gt 0) { [math]::Round(($data.Count - $errors) / $data.Count * 100, 2) } else { 0 }

        if ($csv.Name -match "test1") {
            Write-Host "| API Response (single user)                  |                  |" -ForegroundColor White
            Write-Host "|   Average response time                     | $($avg) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$avg ms".Length) "|" -ForegroundColor White
            Write-Host "|   P95 response time                         | $($p95) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$p95 ms".Length) "|" -ForegroundColor White
        }
        elseif ($csv.Name -match "test2") {
            Write-Host "| Concurrent users (100 threads)              |                  |" -ForegroundColor White
            Write-Host "|   Throughput                                | $($throughput) req/s" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$throughput req/s".Length) "|" -ForegroundColor White
            Write-Host "|   Success rate                              | $($successRate)%" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$successRate%".Length) "|" -ForegroundColor White
            Write-Host "|   P95 response time                         | $($p95) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$p95 ms".Length) "|" -ForegroundColor White
        }
        elseif ($csv.Name -match "test3") {
            $pointsPerReq = 50
            $writeGroups = $data | Group-Object -Property label
            foreach ($wg in $writeGroups) {
                if ($wg.Name -match "100 points") { $pointsPerReq = 100 }
                elseif ($wg.Name -match "500 points") { $pointsPerReq = 500 }
            }
            $pointsThroughput = [math]::Round($throughput * $pointsPerReq, 0)
            Write-Host "| InfluxDB write throughput                   |                  |" -ForegroundColor White
            Write-Host "|   Points/second (sustained)                 | $($pointsThroughput) pts/s" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$pointsThroughput pts/s".Length) "|" -ForegroundColor White
            Write-Host "|   Avg write latency                         | $($avg) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$avg ms".Length) "|" -ForegroundColor White
        }
        elseif ($csv.Name -match "test4") {
            Write-Host "| InfluxDB query latency                      |                  |" -ForegroundColor White
            Write-Host "|   Avg query time                            | $($avg) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$avg ms".Length) "|" -ForegroundColor White
            Write-Host "|   P95 query time                            | $($p95) ms" -ForegroundColor White -NoNewline
            Write-Host "".PadRight(16 - "$p95 ms".Length) "|" -ForegroundColor White
        }
    }

    Write-Host "+---------------------------------------------+------------------+" -ForegroundColor White
}

# --- Main ---
Write-Host "Cesi EMS Benchmark Results Analyzer" -ForegroundColor Cyan
Write-Host "Results directory: $ResultsDir" -ForegroundColor Cyan

if (-not (Test-Path $ResultsDir)) {
    Write-Host "Results directory not found. Run the benchmark first." -ForegroundColor Red
    exit 1
}

$csvFiles = Get-ChildItem $ResultsDir -Filter "*.csv" | Sort-Object Name

if ($csvFiles.Count -eq 0) {
    Write-Host "No CSV result files found in $ResultsDir" -ForegroundColor Red
    exit 1
}

$testNames = @{
    "test1" = "Test 1: API Response Time Baseline (Single User, 100 iterations)"
    "test2" = "Test 2: Concurrent Users Stress Test (100 threads, 5min)"
    "test3" = "Test 3: InfluxDB Write Throughput (10 threads, 50 points/batch)"
    "test4" = "Test 4: InfluxDB Query Latency (5 threads, 3 query types)"
}

foreach ($csv in $csvFiles) {
    $testKey = $testNames.Keys | Where-Object { $csv.Name -match $_ } | Select-Object -First 1
    $testName = if ($testKey) { $testNames[$testKey] } else { $csv.BaseName }
    Analyze-CsvFile -FilePath $csv.FullName -TestName $testName
}

Generate-PaperTable -ResultsDir $ResultsDir
