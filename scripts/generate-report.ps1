# =========================================
# Music Place 테스트 결과 리포트 생성
# =========================================

param(
    [string]$JsonFile = "load-test/results/summary.json"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Music Place Test Report Generator" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

if (!(Test-Path $JsonFile)) {
    Write-Host "❌ Result file not found: $JsonFile" -ForegroundColor Red
    Write-Host "Please run load test first: .\scripts\run-test.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n📄 Analyzing: $JsonFile" -ForegroundColor Yellow

$data = Get-Content $JsonFile | ConvertFrom-Json
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# 메트릭 추출
$totalRequests = $data.metrics.http_reqs.values.count
$failedRate = [math]::Round($data.metrics.http_req_failed.values.rate * 100, 2)
$avgDuration = [math]::Round($data.metrics.http_req_duration.values.avg, 2)
$p50 = [math]::Round($data.metrics.http_req_duration.values.'p(50)', 2)
$p90 = [math]::Round($data.metrics.http_req_duration.values.'p(90)', 2)
$p95 = [math]::Round($data.metrics.http_req_duration.values.'p(95)', 2)
$p99 = [math]::Round($data.metrics.http_req_duration.values.'p(99)', 2)
$rps = [math]::Round($data.metrics.http_reqs.values.rate, 2)
$checkRate = [math]::Round($data.metrics.checks.values.rate * 100, 2)

# 성능 평가
$p95Status = if ($p95 -lt 500) { "✅ 목표 달성" } elseif ($p95 -lt 1000) { "⚠️ 개선 필요" } else { "❌ 목표 미달성" }
$failStatus = if ($failedRate -lt 5) { "✅ 양호" } elseif ($failedRate -lt 10) { "⚠️ 주의" } else { "❌ 위험" }
$overallStatus = if ($failedRate -lt 5 -and $p95 -lt 500) { "✅ 테스트 통과" } elseif ($failedRate -lt 10 -and $p95 -lt 1000) { "⚠️ 개선 필요" } else { "❌ 목표 미달성" }

# HTML 리포트 생성
$htmlReport = @"
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Music Place Load Test Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }
        .header h1 {
            font-size: 36px;
            margin-bottom: 10px;
        }
        .header p {
            opacity: 0.9;
            font-size: 16px;
        }
        .content {
            padding: 40px;
        }
        .status-banner {
            background: #f8f9fa;
            border-left: 5px solid #28a745;
            padding: 20px;
            margin-bottom: 30px;
            border-radius: 8px;
        }
        .status-banner.warning {
            border-left-color: #ffc107;
        }
        .status-banner.danger {
            border-left-color: #dc3545;
        }
        .status-banner h2 {
            color: #2c3e50;
            margin-bottom: 10px;
        }
        .metrics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }
        .metric-card {
            background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
            padding: 25px;
            border-radius: 12px;
            border: 2px solid #e9ecef;
            transition: transform 0.2s;
        }
        .metric-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
        }
        .metric-title {
            font-size: 14px;
            color: #6c757d;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        .metric-value {
            font-size: 32px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 5px;
        }
        .metric-unit {
            font-size: 14px;
            color: #95a5a6;
        }
        .pass { color: #28a745; }
        .warning { color: #ffc107; }
        .fail { color: #dc3545; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 30px 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            border-radius: 8px;
            overflow: hidden;
        }
        th {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }
        td {
            padding: 15px;
            border-bottom: 1px solid #e9ecef;
        }
        tr:hover {
            background: #f8f9fa;
        }
        .footer {
            background: #f8f9fa;
            padding: 30px;
            text-align: center;
            color: #6c757d;
            border-top: 1px solid #e9ecef;
        }
        .recommendations {
            background: #fff3cd;
            border-left: 5px solid #ffc107;
            padding: 20px;
            margin: 30px 0;
            border-radius: 8px;
        }
        .recommendations h3 {
            color: #856404;
            margin-bottom: 15px;
        }
        .recommendations ul {
            list-style-position: inside;
            color: #856404;
        }
        .recommendations li {
            margin: 10px 0;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎵 Music Place Load Test Report</h1>
            <p>Performance Analysis & Recommendations</p>
            <p><strong>생성 시간:</strong> $timestamp</p>
        </div>

        <div class="content">
            <div class="status-banner $(if ($overallStatus -match '통과') { '' } elseif ($overallStatus -match '개선') { 'warning' } else { 'danger' })">
                <h2>$overallStatus</h2>
                <p>전체 성능 평가 결과</p>
            </div>

            <h2 style="color: #2c3e50; margin-bottom: 20px;">📊 주요 메트릭</h2>
            <div class="metrics">
                <div class="metric-card">
                    <div class="metric-title">총 요청 수</div>
                    <div class="metric-value">$totalRequests</div>
                    <span class="metric-unit">requests</span>
                </div>
                <div class="metric-card">
                    <div class="metric-title">처리량 (RPS)</div>
                    <div class="metric-value">$rps</div>
                    <span class="metric-unit">req/s</span>
                </div>
                <div class="metric-card">
                    <div class="metric-title">평균 응답 시간</div>
                    <div class="metric-value">$avgDuration</div>
                    <span class="metric-unit">ms</span>
                </div>
                <div class="metric-card">
                    <div class="metric-title">P95 응답 시간</div>
                    <div class="metric-value $(if ($p95 -lt 500) { 'pass' } elseif ($p95 -lt 1000) { 'warning' } else { 'fail' })">$p95</div>
                    <span class="metric-unit">ms - $p95Status</span>
                </div>
                <div class="metric-card">
                    <div class="metric-title">에러율</div>
                    <div class="metric-value $(if ($failedRate -lt 5) { 'pass' } elseif ($failedRate -lt 10) { 'warning' } else { 'fail' })">$failedRate</div>
                    <span class="metric-unit">% - $failStatus</span>
                </div>
                <div class="metric-card">
                    <div class="metric-title">체크 통과율</div>
                    <div class="metric-value pass">$checkRate</div>
                    <span class="metric-unit">%</span>
                </div>
            </div>

            <h2 style="color: #2c3e50; margin: 40px 0 20px;">📈 응답 시간 분포</h2>
            <table>
                <tr>
                    <th>백분위수</th>
                    <th>응답 시간 (ms)</th>
                    <th>상태</th>
                </tr>
                <tr>
                    <td>P50 (중간값)</td>
                    <td>$p50</td>
                    <td class="$(if ($p50 -lt 200) { 'pass' } else { 'warning' })">$(if ($p50 -lt 200) { '✅ 양호' } else { '⚠️ 주의' })</td>
                </tr>
                <tr>
                    <td>P90</td>
                    <td>$p90</td>
                    <td class="$(if ($p90 -lt 400) { 'pass' } else { 'warning' })">$(if ($p90 -lt 400) { '✅ 양호' } else { '⚠️ 주의' })</td>
                </tr>
                <tr>
                    <td>P95</td>
                    <td>$p95</td>
                    <td class="$(if ($p95 -lt 500) { 'pass' } else { 'fail' })">$(if ($p95 -lt 500) { '✅ 목표 달성' } else { '❌ 목표 미달성' })</td>
                </tr>
                <tr>
                    <td>P99</td>
                    <td>$p99</td>
                    <td class="$(if ($p99 -lt 1000) { 'pass' } else { 'fail' })">$(if ($p99 -lt 1000) { '✅ 목표 달성' } else { '❌ 목표 미달성' })</td>
                </tr>
            </table>

            <div class="recommendations">
                <h3>💡 성능 개선 권장사항</h3>
                <ul>
                    $(if ($p95 -gt 500) { "<li><strong>응답 시간 개선:</strong> P95가 500ms를 초과합니다. DB 쿼리 최적화, 인덱스 추가, 캐싱 전략을 검토하세요.</li>" } else { "" })
                    $(if ($failedRate -gt 5) { "<li><strong>에러율 감소:</strong> 에러율이 5%를 초과합니다. 로그를 확인하고 예외 처리를 강화하세요.</li>" } else { "" })
                    $(if ($rps -lt 50) { "<li><strong>처리량 증가:</strong> RPS가 낮습니다. 스레드 풀, DB 커넥션 풀 크기를 조정하세요.</li>" } else { "" })
                    <li><strong>Grafana 대시보드 확인:</strong> HikariCP, JVM Heap, GC 메트릭을 상세히 분석하세요.</li>
                    <li><strong>느린 API 식별:</strong> Prometheus에서 uri별 응답 시간을 확인하여 병목 API를 찾으세요.</li>
                    <li><strong>DB 쿼리 최적화:</strong> EXPLAIN을 사용하여 느린 쿼리를 분석하고 인덱스를 추가하세요.</li>
                </ul>
            </div>
        </div>

        <div class="footer">
            <p><strong>Music Place Backend - Load Test Report</strong></p>
            <p>Generated by k6 Performance Testing Tool</p>
            <p style="margin-top: 10px;">
                <a href="http://localhost:3000" target="_blank" style="color: #667eea; text-decoration: none;">📊 View Grafana Dashboard</a> |
                <a href="http://localhost:9090" target="_blank" style="color: #667eea; text-decoration: none;">🔍 View Prometheus</a>
            </p>
        </div>
    </div>
</body>
</html>
"@

$reportFile = "load-test/results/report-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"
$htmlReport | Out-File -FilePath $reportFile -Encoding UTF8

Write-Host "`n✅ 리포트 생성 완료!" -ForegroundColor Green
Write-Host "📄 파일: $reportFile" -ForegroundColor Cyan

# 브라우저에서 열기
Start-Process $reportFile

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Report Summary:" -ForegroundColor Yellow
Write-Host "  Total Requests: $totalRequests" -ForegroundColor White
Write-Host "  Failed Rate: $failedRate% - $failStatus" -ForegroundColor $(if ($failedRate -lt 5) { 'Green' } else { 'Red' })
Write-Host "  P95 Latency: ${p95}ms - $p95Status" -ForegroundColor $(if ($p95 -lt 500) { 'Green' } else { 'Red' })
Write-Host "  Overall: $overallStatus" -ForegroundColor $(if ($overallStatus -match '통과') { 'Green' } elseif ($overallStatus -match '개선') { 'Yellow' } else { 'Red' })
Write-Host "==========================================" -ForegroundColor Cyan
