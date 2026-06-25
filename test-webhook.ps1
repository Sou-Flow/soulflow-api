$payload = '{"id": 123, "code": "DH123", "transferAmount": 100000, "content": "DH123"}'
$secret = "whsec_4tK1AgeTT1QohzWyWZAJLPLsptg8mMDM"

$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$hash = $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($payload))
$signature = [BitConverter]::ToString($hash).Replace("-", "").ToLower()

Write-Host "Computed Signature: $signature"
