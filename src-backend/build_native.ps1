param (
    [string]$graalvm_home = "C:\Users\Violet\scoop\apps\graalvm\current",
    [string]$vcvars_path = "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvars64.bat"
)

$env:JAVA_HOME = $graalvm_home
$env:GRAALVM_HOME = $graalvm_home

Write-Host ">>> Loading MSVC environment from $vcvars_path..."

# Run vcvars and get all environment variables
$vars = cmd /c "call `"$vcvars_path`" & set"

# Parse and set each variable in current PowerShell session
foreach ($var in $vars) {
    if ($var -match "^(.*?)=(.*)$") {
        $name = $matches[1]
        $value = $matches[2]
        if ($name -match "^(PATH|INCLUDE|LIB|LIBPATH|DevEnvDir|VSINSTALLDIR|VisualStudioVersion|VCINSTALLDIR|WindowsSdkDir|FrameworkDir|FrameworkVersion|FrameworkSDKDir|VCToolsInstallDir|WindowsSdkBinPath|WindowsLibPath|WindowsSdkVerBinPath)$") {
            if ($name -eq "PATH") {
                $env:PATH = "$value;$env:PATH"
            } else {
                Set-Item -Path "Env:$name" -Value $value
            }
        }
    }
}

# Verify cl.exe
$clPath = where.exe cl.exe
if ($clPath) {
    Write-Host ">>> Found cl.exe at: $clPath"
} else {
    Write-Error ">>> cl.exe not found! MSVC environment not correctly loaded."
    exit 1
}

# Run the actual build
Write-Host ">>> Starting gradle nativeCompile..."
$buildResult = gradle nativeCompile $args
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host ">>> Build successful! Post-processing executable..."
    
    # Define paths
    $projectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
    $sourceExe = Join-Path $projectRoot "src-backend\build\native\nativeCompile\src-backend.exe"
    $targetDir = Join-Path $projectRoot "src-tauri\binaries"
    
    # Get target triple from rustc
    $targetTriple = (rustc -Vv | Select-String "host:").ToString().Split(" ")[1]
    $targetExe = Join-Path $targetDir "tulipe-backend-$targetTriple.exe"
    
    # Check if source executable exists
    if (Test-Path $sourceExe) {
        # Create target directory if it doesn't exist
        if (-not (Test-Path $targetDir)) {
            Write-Host ">>> Creating binaries directory: $targetDir"
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }
        
        # Copy and rename the executable
        Write-Host ">>> Copying executable to: $targetExe"
        Copy-Item -Path $sourceExe -Destination $targetExe -Force
        
        # Verify the copy was successful
        if (Test-Path $targetExe) {
            $fileInfo = Get-Item $targetExe
            Write-Host ">>> ✓ Successfully copied executable (${($fileInfo.Length / 1MB):F2} MB)"
            Write-Host ">>> Target location: $targetExe"
        } else {
            Write-Error ">>> Failed to copy executable to target location"
            exit 1
        }
    } else {
        Write-Error ">>> Source executable not found at: $sourceExe"
        exit 1
    }
} else {
    Write-Error ">>> Build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}
