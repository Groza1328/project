$ok = (Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue).TcpTestSucceeded
if ($ok) { exit 0 }
exit 1
