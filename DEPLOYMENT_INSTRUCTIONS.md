# 🚀 DEPLOYMENT INSTRUCTIONS

## To Deploy the GraphQL Schema Generation Fix:

### Step 1: Run PowerShell as Administrator

1. Press `Win + X` and select "Windows PowerShell (Admin)"
   OR
2. Search for "PowerShell" → Right-click → "Run as administrator"
3. Accept the User Account Control prompt

### Step 2: Navigate to the Tools Directory

```powershell
cd "d:\Application files - Do not delete\github\ballerina-library\graphql-tools"
```

### Step 3: Deploy the Fix

```powershell
.\deploy_simple.ps1
```

### Expected Output:

```
🚀 GraphQL Schema Generator Fix Deployment
===========================================
✅ Running with administrator privileges
📁 Source: graphql-schema-file-generator-0.13.0-fixed.jar
📂 Target: C:\Program Files\Ballerina\distributions\ballerina-2201.12.7\bre\lib\graphql-schema-file-generator-0.13.0.jar
💾 Creating backup...
✅ Backup created: C:\Program Files\Ballerina\distributions\ballerina-2201.12.7\bre\lib\graphql-schema-file-generator-0.13.0.jar.backup
🔄 Deploying fixed JAR...
✅ Successfully deployed!
✅ Verification: Size 15843 bytes
🎉 DEPLOYMENT COMPLETE!
🧪 Test with: bal graphql -i service.bal -o .
```

### Step 4: Test the Fix

Navigate to any directory with a Ballerina GraphQL service and run:

```powershell
bal graphql -i service.bal -o .
```

You should now see the improved UX:

- Immediate file conflict detection (no wasted processing)
- User choice is respected ("No" means no unwanted files)
- Clean exit with proper messages

---

**The fix is ready to deploy! Just follow the steps above.** 🎯
