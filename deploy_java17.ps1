# Deploy Java 17 Compatible Version
Write-Host "🔄 Deploying Java 17 compatible fix..." -ForegroundColor Green

$fixedJar = "graphql-schema-file-generator-0.13.0-java17.jar"
$targetJar = "C:\Program Files\Ballerina\distributions\ballerina-2201.12.7\bre\lib\graphql-schema-file-generator-0.13.0.jar"

Copy-Item $fixedJar $targetJar -Force
Write-Host "✅ Java 17 compatible version deployed!" -ForegroundColor Green
Write-Host "🧪 Ready to test!" -ForegroundColor Cyan