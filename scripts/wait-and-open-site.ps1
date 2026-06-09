param(
    [int]$Port = 8080,
    [string]$Url = "http://localhost:8080/"
)

$deadline = (Get-Date).AddMinutes(6)

while ((Get-Date) -lt $deadline) {
    $up = (Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue).TcpTestSucceeded
    if ($up) {
        Start-Process $Url
        exit 0
    }
    Start-Sleep -Seconds 2
}

exit 1
