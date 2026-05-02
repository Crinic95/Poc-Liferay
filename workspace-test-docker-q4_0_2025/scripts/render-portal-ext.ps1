param(
    [string]$EnvFile = "env\local.env",
    [string]$TemplateFile = "bundles\portal-ext.template.properties",
    [string]$OutputFile = "bundles\portal-ext.properties"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $EnvFile)) {
    Write-Error "File env non trovato: $EnvFile"
}

if (!(Test-Path $TemplateFile)) {
    Write-Error "Template non trovato: $TemplateFile"
}

$envVars = @{}

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }

    $parts = $line -split "=", 2

    if ($parts.Count -eq 2) {
        $key = $parts[0].Trim()
        $value = $parts[1].Trim()
        $envVars[$key] = $value
    }
}

$content = Get-Content $TemplateFile -Raw

foreach ($key in $envVars.Keys) {
    $placeholder = '${' + $key + '}'
    $content = $content.Replace($placeholder, $envVars[$key])
}

Set-Content -Path $OutputFile -Value $content -Encoding UTF8

Write-Host "Generato: $OutputFile"