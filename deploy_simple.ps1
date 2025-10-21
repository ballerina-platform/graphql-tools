# GraphQL Schema Generator Fix Deployment Script
param([switch]$Force = $false)

$ErrorActionPreference = "Stop"

Write-Host "🚀 GraphQL Schema Generator Fix Deployment" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green

# Define paths
$fixedJar = "graphql-schema-file-generator-0.13.0-fixed.jar"
$ballerinaDir = "C:\Program Files\Ballerina\distributions\ballerina-2201.12.7\bre\lib"
$targetJar = "$ballerinaDir\graphql-schema-file-generator-0.13.0.jar"
$backupJar = "$ballerinaDir\graphql-schema-file-generator-0.13.0.jar.backup"

# Check if running as administrator
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ This script requires administrator privileges!" -ForegroundColor Red
    Write-Host "   Please run PowerShell as Administrator and try again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Running with administrator privileges" -ForegroundColor Green

# Verify source file exists
if (-not (Test-Path $fixedJar)) {
    Write-Host "❌ Fixed JAR not found: $fixedJar" -ForegroundColor Red
    exit 1
}

Write-Host "📁 Source: $fixedJar" -ForegroundColor Cyan
Write-Host "📂 Target: $targetJar" -ForegroundColor Cyan

# Create backup if original exists
if (Test-Path $targetJar) {
    Write-Host "💾 Creating backup..." -ForegroundColor Yellow
    Copy-Item $targetJar $backupJar -Force
    Write-Host "✅ Backup created: $backupJar" -ForegroundColor Green
}

# Deploy the fix
Write-Host "🔄 Deploying fixed JAR..." -ForegroundColor Green
Copy-Item $fixedJar $targetJar -Force
Write-Host "✅ Successfully deployed!" -ForegroundColor Green

# Verify
if (Test-Path $targetJar) {
    $deployedFile = Get-Item $targetJar
    Write-Host "✅ Verification: Size $($deployedFile.Length) bytes" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎉 DEPLOYMENT COMPLETE!" -ForegroundColor Green
Write-Host "🧪 Test with: bal graphql -i service.bal -o ." -ForegroundColor Cyan