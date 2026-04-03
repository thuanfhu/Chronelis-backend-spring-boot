$base = "http://localhost:8080"
$password = "Chronelis123@"

function Login([string]$email) {
  $payload = @{ email = $email; password = $password } | ConvertTo-Json
  try {
    $resp = Invoke-RestMethod -Method Post -Uri "$base/api/v1/auth/login" -ContentType "application/json" -Body $payload
    return $resp.data.accessToken
  } catch {
    if ($_.Exception.Response) {
      $status = [int]$_.Exception.Response.StatusCode
      Write-Host "LOGIN_FAIL $email => $status"
    } else {
      Write-Host "LOGIN_FAIL $email => $($_.Exception.Message)"
    }
    return $null
  }
}

function Hit([string]$label, [string]$token, [string]$path) {
  $headers = @{ Authorization = "Bearer $token" }
  try {
    $resp = Invoke-WebRequest -Method Get -Uri "$base$path" -Headers $headers -UseBasicParsing
    Write-Host "[$label] GET $path => $($resp.StatusCode)"
  } catch {
    if ($_.Exception.Response) {
      $status = [int]$_.Exception.Response.StatusCode
      $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
      $body = $reader.ReadToEnd()
      Write-Host "[$label] GET $path => $status"
      Write-Host "[$label] BODY $body"
    } else {
      Write-Host "[$label] GET $path => ERROR $($_.Exception.Message)"
    }
  }
}

$adminToken = Login "chronelis.admin@gmail.com"
$userToken = Login "chronelis.user@gmail.com"

if ($adminToken) {
  Hit "ADMIN" $adminToken "/api/v1/workspaces"
}

if ($userToken) {
  Hit "USER" $userToken "/api/v1/workspaces"
  Hit "USER" $userToken "/api/v1/roles?page=1&pageSize=10"
}
