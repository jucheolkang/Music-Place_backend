# =========================================
# Music Place Test Report Generator
# warmup / load / stress 모두 지원
# =========================================

param(
    [ValidateSet("warmup", "load", "stress")]
    [string]$Type = "load"
)

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Music Place Test Report Generator"
Write-Host "==========================================" -ForegroundColor Cyan

$resultDir = "load-test/results"

switch ($Type) {

    "warmup" {
        $pattern = "warmup-*.json"
    }

    "load" {
        $pattern = "load-test-*.json"
    }

    "stress" {
        $pattern = "stress-test-*.json"
    }
}

$jsonFile = Get-ChildItem $resultDir -Filter $pattern |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1

if ($null -eq $jsonFile) {

    Write-Host ""
    Write-Host "Result file not found." -ForegroundColor Red
    Write-Host ""
    exit
}

Write-Host ""
Write-Host "Analyzing : $($jsonFile.Name)" -ForegroundColor Yellow

$data = Get-Content $jsonFile.FullName | ConvertFrom-Json

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# ======================================================
# Metric
# ======================================================

$totalRequests = [int]$data.metrics.http_reqs.values.count

$failedRate =
    [math]::Round(
        $data.metrics.http_req_failed.values.rate * 100,
        2
    )

$rps =
    [math]::Round(
        $data.metrics.http_reqs.values.rate,
        2
    )

$avg =
    [math]::Round(
        $data.metrics.http_req_duration.values.avg,
        2
    )

$p50 =
    [math]::Round(
        $data.metrics.http_req_duration.values.'p(50)',
        2
    )

$p90 =
    [math]::Round(
        $data.metrics.http_req_duration.values.'p(90)',
        2
    )

$p95 =
    [math]::Round(
        $data.metrics.http_req_duration.values.'p(95)',
        2
    )

$p99 =
    [math]::Round(
        $data.metrics.http_req_duration.values.'p(99)',
        2
    )

$checkRate = 0

if ($data.metrics.checks) {

    $checkRate =
        [math]::Round(
            $data.metrics.checks.values.rate * 100,
            2
        )
}

$dbOperations = 0

if ($data.metrics.db_operations) {

    $dbOperations =
        $data.metrics.db_operations.values.count
}

$authFailures = 0

if ($data.metrics.auth_failures) {

    $authFailures =
        $data.metrics.auth_failures.values.count
}

# ======================================================
# Evaluation
# ======================================================

if ($failedRate -lt 5) {

    $failStatus = "GOOD"

}
elseif ($failedRate -lt 10) {

    $failStatus = "WARNING"

}
else {

    $failStatus = "BAD"

}

if ($p95 -lt 500) {

    $latencyStatus = "GOOD"

}
elseif ($p95 -lt 1000) {

    $latencyStatus = "WARNING"

}
else {

    $latencyStatus = "BAD"

}

if (($failedRate -lt 5) -and ($p95 -lt 500)) {

    $overall = "PASS"

}
elseif (($failedRate -lt 10) -and ($p95 -lt 1000)) {

    $overall = "WARNING"

}
else {

    $overall = "FAIL"

}

# ======================================================
# HTML
# ======================================================

$html = @"
<!DOCTYPE html>

<html>

<head>

<meta charset="utf-8">

<title>Music Place Test Report</title>

<style>

body{

font-family:Arial;

margin:40px;

background:#f5f5f5;

}

table{

border-collapse:collapse;

width:800px;

background:white;

}

th,td{

padding:12px;

border:1px solid #ccc;

text-align:left;

}

th{

background:#4CAF50;

color:white;

}

h1{

color:#333;

}

.status{

font-size:22px;

font-weight:bold;

margin-bottom:20px;

}

</style>

</head>

<body>

<h1>Music Place Performance Report</h1>

<p>Generated : $timestamp</p>

<p class="status">

Overall : $overall

</p>

<table>

<tr>

<th>Metric</th>

<th>Value</th>

</tr>

<tr>

<td>Total Requests</td>

<td>$totalRequests</td>

</tr>

<tr>

<td>RPS</td>

<td>$rps</td>

</tr>

<tr>

<td>Average</td>

<td>${avg} ms</td>

</tr>

<tr>

<td>P50</td>

<td>${p50} ms</td>

</tr>

<tr>

<td>P90</td>

<td>${p90} ms</td>

</tr>

<tr>

<td>P95</td>

<td>${p95} ms ($latencyStatus)</td>

</tr>

<tr>

<td>P99</td>

<td>${p99} ms</td>

</tr>

<tr>

<td>Failure Rate</td>

<td>$failedRate % ($failStatus)</td>

</tr>

<tr>

<td>Check Success</td>

<td>$checkRate %</td>

</tr>

<tr>

<td>DB Operations</td>

<td>$dbOperations</td>

</tr>

<tr>

<td>Auth Failures</td>

<td>$authFailures</td>

</tr>

</table>

</body>

</html>

"@

$reportName = "report-$Type-$((Get-Date).ToString('yyyyMMdd-HHmmss')).html"

$reportPath = Join-Path $resultDir $reportName

$html | Out-File $reportPath -Encoding UTF8

Write-Host ""
Write-Host "=========================================="
Write-Host "Summary"
Write-Host "=========================================="

Write-Host "Test Type      : $Type"
Write-Host "Result File    : $($jsonFile.Name)"
Write-Host "Total Requests : $totalRequests"
Write-Host "RPS            : $rps"
Write-Host "Average        : $avg ms"
Write-Host "P95            : $p95 ms"
Write-Host "P99            : $p99 ms"
Write-Host "Failure Rate   : $failedRate %"
Write-Host "Check Success  : $checkRate %"
Write-Host "DB Operations  : $dbOperations"
Write-Host "Auth Failures  : $authFailures"
Write-Host "Overall        : $overall"

Write-Host ""
Write-Host "HTML Report"
Write-Host "$reportPath"
Write-Host ""

Start-Process $reportPath
