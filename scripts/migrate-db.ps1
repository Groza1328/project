param(
    [string]$PgUser = "postgres",
    [string]$PgHost = "127.0.0.1",
    [int]$PgPort = 5432,
    [string]$Database = "sibmobile",
    [string]$PgPassword = ""
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptDir "migrate-db.sql"

$psql = $null
$candidates = @(
    "$env:ProgramFiles\PostgreSQL\17\bin\psql.exe",
    "$env:ProgramFiles\PostgreSQL\16\bin\psql.exe",
    "$env:ProgramFiles\PostgreSQL\15\bin\psql.exe"
)
foreach ($path in $candidates) {
    if (Test-Path $path) {
        $psql = $path
        break
    }
}
if (-not $psql) {
    $cmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($cmd) { $psql = $cmd.Source }
}

if (-not $psql) {
    Write-Host "psql not found - skipping schema migration."
    exit 0
}

if ($PgPassword) {
    $env:PGPASSWORD = $PgPassword
} elseif ($env:PGPASSWORD) {
    $PgPassword = $env:PGPASSWORD
}

& $psql -U $PgUser -h $PgHost -p $PgPort -d $Database -v ON_ERROR_STOP=1 -f $sqlFile
if ($LASTEXITCODE -ne 0) {
    Write-Error "Schema migration failed."
    exit 1
}

Write-Host "Schema migration OK."
exit 0
