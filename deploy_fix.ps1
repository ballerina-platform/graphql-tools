# GraphQL Schema Generator Fix Deployment Script
# This script deploys our fix to the Ballerina installation

param(
    [switch]$Force = $false
)

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
    Write-Host ""
    Write-Host "🔧 To run as admin:" -ForegroundColor Cyan
    Write-Host "   1. Right-click on PowerShell" -ForegroundColor White
    Write-Host "   2. Select 'Run as administrator'" -ForegroundColor White
    Write-Host "   3. Navigate to this directory and run: .\deploy_fix.ps1" -ForegroundColor White
    exit 1
}

Write-Host "✅ Running with administrator privileges" -ForegroundColor Green
Write-Host ""

# Verify source file exists
if (-not (Test-Path $fixedJar)) {
    Write-Host "❌ Fixed JAR not found: $fixedJar" -ForegroundColor Red
    exit 1
}

Write-Host "📁 Source JAR: $fixedJar" -ForegroundColor Cyan
Write-Host "📂 Target location: $targetJar" -ForegroundColor Cyan
Write-Host ""

# Check if target exists and create backup
if (Test-Path $targetJar) {
    Write-Host "💾 Creating backup of original JAR..." -ForegroundColor Yellow
    
    if (Test-Path $backupJar) {
        if ($Force) {
            Remove-Item $backupJar -Force
            Write-Host "   Replaced existing backup" -ForegroundColor Gray
        } else {
            Write-Host "⚠️  Backup already exists: $backupJar" -ForegroundColor Yellow
            $response = Read-Host "   Replace existing backup? [y/N]"
            if ($response -match '^[Yy]') {
                Remove-Item $backupJar -Force
                Write-Host "   Existing backup replaced" -ForegroundColor Gray
            } else {
                Write-Host "   Keeping existing backup" -ForegroundColor Gray
            }
        }
    }
    
    if (-not (Test-Path $backupJar)) {
        Copy-Item $targetJar $backupJar -Force
        Write-Host "✅ Backup created: $backupJar" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️  Original JAR not found at expected location" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🔄 Deploying fixed JAR..." -ForegroundColor Green

try {
    # Copy the fixed JAR to replace the original
    Copy-Item $fixedJar $targetJar -Force
    Write-Host "✅ Successfully deployed fixed JAR!" -ForegroundColor Green
    
    # Verify deployment
    if (Test-Path $targetJar) {
        $deployedFile = Get-Item $targetJar
        Write-Host "✅ Verification successful" -ForegroundColor Green
        Write-Host "   File: $($deployedFile.Name)" -ForegroundColor Gray
        Write-Host "   Size: $($deployedFile.Length) bytes" -ForegroundColor Gray
        Write-Host "   Modified: $($deployedFile.LastWriteTime)" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "🎉 DEPLOYMENT COMPLETE!" -ForegroundColor Green
    Write-Host "========================" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔧 The GraphQL schema generation UX fix is now active!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "✨ New behavior:" -ForegroundColor Yellow
    Write-Host "   • Early file conflict detection" -ForegroundColor White
    Write-Host "   • Respects user choice (No = no files created)" -ForegroundColor White
    Write-Host "   • No wasted computation time" -ForegroundColor White
    Write-Host "   • Clean directory management" -ForegroundColor White
    Write-Host ""
    Write-Host "🧪 Test it with: bal graphql -i service.bal -o ." -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Deployment failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔍 Troubleshooting:" -ForegroundColor Yellow
    Write-Host "   • Ensure Ballerina is not running" -ForegroundColor White
    Write-Host "   • Check file permissions" -ForegroundColor White
    Write-Host "   • Verify the target directory exists" -ForegroundColor White
    exit 1
}

Write-Host ""
Write-Host "📋 Summary:" -ForegroundColor Green
Write-Host "   Original: $backupJar" -ForegroundColor Gray
Write-Host "   Fixed:    $targetJar" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 To rollback: Copy backup over the current file" -ForegroundColor Yellow