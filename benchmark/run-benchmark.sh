#!/usr/bin/env bash
# Cesi EMS Performance Benchmark Runner (bash wrapper)
# Usage: ./benchmark/run-benchmark.sh /path/to/jmeter [test_number]
#   e.g. ./benchmark/run-benchmark.sh C:/apache-jmeter-5.6.3
#   e.g. ./benchmark/run-benchmark.sh C:/apache-jmeter-5.6.3 3

set -e

JMETER_HOME="${1:?Usage: $0 <jmeter-home> [test-number]}"
TEST_ONLY="${2:-0}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JMX="$SCRIPT_DIR/jmeter/cesi-ems-benchmark.jmx"
RESULTS_DIR="$SCRIPT_DIR/results"
JMETER_BIN="$JMETER_HOME/bin/jmeter"

# On Windows, use .bat
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || -f "$JMETER_HOME/bin/jmeter.bat" ]]; then
    JMETER_BIN="$JMETER_HOME/bin/jmeter.bat"
fi

if [[ ! -f "$JMETER_BIN" ]]; then
    echo "ERROR: JMeter not found at $JMETER_BIN"
    exit 1
fi

mkdir -p "$RESULTS_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "============================================"
echo " Cesi EMS Performance Benchmark"
echo " Timestamp: $TIMESTAMP"
echo "============================================"

# Pre-flight checks
echo ""
echo "[Pre-flight] Checking backend..."
if curl -s --max-time 5 http://127.0.0.1:8080/captchaImage > /dev/null 2>&1; then
    echo "  Backend is running."
else
    echo "  WARNING: Backend not reachable at http://127.0.0.1:8080"
fi

echo "[Pre-flight] Checking InfluxDB..."
if curl -s --max-time 5 http://127.0.0.1:8086/health > /dev/null 2>&1; then
    echo "  InfluxDB is running."
else
    echo "  WARNING: InfluxDB not reachable at http://127.0.0.1:8086"
fi

RESULT_FILE="$RESULTS_DIR/all_${TIMESTAMP}.csv"
LOG_FILE="$RESULTS_DIR/all_${TIMESTAMP}.log"
REPORT_DIR="$RESULTS_DIR/report_${TIMESTAMP}"

echo ""
echo "--- Running all tests ---"
echo "  JMX: $JMX"
echo "  Results: $RESULT_FILE"
echo ""

"$JMETER_BIN" -n -t "$JMX" -l "$RESULT_FILE" -j "$LOG_FILE" -e -o "$REPORT_DIR"

echo ""
echo "============================================"
echo " Benchmark Complete"
echo " CSV Results : $RESULT_FILE"
echo " HTML Report : $REPORT_DIR/index.html"
echo " Log         : $LOG_FILE"
echo "============================================"
