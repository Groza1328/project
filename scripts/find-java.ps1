$roots = @(
    $env:JAVA_HOME,
    "$env:ProgramFiles\Eclipse Adoptium",
    "$env:ProgramFiles\Java",
    "$env:ProgramFiles\Microsoft\jdk*",
    "$env:ProgramFiles\Amazon Corretto",
    "$env:LocalAppData\Programs\Eclipse Adoptium"
) | Where-Object { $_ -and (Test-Path $_) }

foreach ($root in $roots) {
    $candidates = @()
    if ($root -match '\\bin$') {
        $candidates = @(Split-Path $root -Parent)
    } else {
        $candidates = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue
    }

    foreach ($dir in $candidates) {
        $java = Join-Path $dir.FullName "bin\java.exe"
        if (-not $java) { $java = Join-Path $dir "bin\java.exe" }
        if (Test-Path $java) {
            Write-Output (Split-Path $java -Parent)
            exit 0
        }
    }
}

exit 1
