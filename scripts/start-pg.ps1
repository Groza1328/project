param(
    [string]$ServiceName = "postgresql-x64-17"
)

try {
    Start-Service -Name $ServiceName -ErrorAction Stop
    exit 0
} catch {
    exit 1
}
