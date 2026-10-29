param([switch]$Pruebas)
$ErrorActionPreference = 'Stop'

# Busca un JDK 21 local si JAVA_HOME todavía no está configurado.
if (-not $env:JAVA_HOME) {
    $taskJdkRoots = @((Join-Path $env:USERPROFILE '.jdks'), (Join-Path $env:ProgramFiles 'Java'))
    $taskJdk = Get-ChildItem -LiteralPath $taskJdkRoots -Directory -ErrorAction SilentlyContinue |
        Where-Object {
            $taskRelease = Join-Path $_.FullName 'release'
            (Test-Path -LiteralPath $taskRelease) -and
                (Select-String -LiteralPath $taskRelease -Pattern '^JAVA_VERSION="21\.' -Quiet)
        } | Select-Object -First 1
    if ($taskJdk) { $env:JAVA_HOME = $taskJdk.FullName }
}
if (-not $env:JAVA_HOME) { throw 'Configura JAVA_HOME apuntando a tu JDK 21.' }
$env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH

Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    if ($Pruebas) { & .\mvnw.cmd test }
    else { & .\mvnw.cmd spring-boot:run }
    $taskExit = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $taskExit
