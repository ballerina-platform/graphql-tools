# Deploy Instant File Conflict Check Fix
Write-Host "🚀 Deploying INSTANT file conflict check fix..." -ForegroundColor Green

$fixedJar = "graphql-schema-file-generator\graphql-schema-file-generator-0.13.0-instant-fix.jar"
$targetJar = "C:\Program Files\Ballerina\distributions\ballerina-2201.12.7\bre\lib\graphql-schema-file-generator-0.13.0.jar"

Copy-Item $fixedJar $targetJar -Force
Write-Host "✅ INSTANT FIX DEPLOYED!" -ForegroundColor Green
Write-Host ""
Write-Host "🎯 NEW BEHAVIOR:" -ForegroundColor Cyan
Write-Host "   • File conflict check happens IMMEDIATELY" -ForegroundColor White
Write-Host "   • No waiting for expensive processing" -ForegroundColor White
Write-Host "   • Instant 'No' response = instant exit" -ForegroundColor White
Write-Host ""
Write-Host "🧪 Test with: bal graphql -i service.bal -o ." -ForegroundColor Yellow