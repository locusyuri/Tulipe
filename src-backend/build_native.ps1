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
gradle nativeCompile $args
