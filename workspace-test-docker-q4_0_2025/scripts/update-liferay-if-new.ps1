param(
    [string]$EnvFile = ".env",
    [string]$ServiceName = "liferay",
    [string]$Repository = "liferay/dxp",
    [string]$AllowedLine = "",
    [string[]]$ComposeFiles = @("docker-compose.yml", "docker-compose.dev.yml")
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $EnvFile)) {
    Write-Error "File non trovato: $EnvFile"
}

$currentImageLine = Get-Content $EnvFile | Where-Object { $_ -match "^LIFERAY_IMAGE=" } | Select-Object -First 1

if (!$currentImageLine) {
    Write-Error "LIFERAY_IMAGE non trovato in $EnvFile"
}

$currentImage = ($currentImageLine -split "=", 2)[1].Trim()
$currentTag = ($currentImage -split ":")[-1]

Write-Host "Immagine attuale: $currentImage"
Write-Host "Tag attuale:      $currentTag"
Write-Host ""

$pageUrl = "https://hub.docker.com/v2/repositories/$Repository/tags?page_size=100"
$allTags = @()

while ($pageUrl) {
    $response = Invoke-RestMethod -Uri $pageUrl -Method Get
    $allTags += $response.results.name
    $pageUrl = $response.next
}

$tagObjects = @()

foreach ($tag in $allTags) {
    if ($AllowedLine -ne "") {
        $escapedLine = [regex]::Escape($AllowedLine)
        $pattern = "^$escapedLine\.([0-9]+)(-lts)?$"
    }
    else {
        $pattern = "^([0-9]{4})\.q([0-9]+)\.([0-9]+)(-lts)?$"
    }

    if ($tag -match $pattern) {
        if ($AllowedLine -ne "") {
            $lineParts = $AllowedLine -split "\.q"
            $year = [int]$lineParts[0]
            $quarter = [int]$lineParts[1]
            $patch = [int]$Matches[1]
        }
        else {
            $year = [int]$Matches[1]
            $quarter = [int]$Matches[2]
            $patch = [int]$Matches[3]
        }

        $tagObjects += [PSCustomObject]@{
            Tag = $tag
            Year = $year
            Quarter = $quarter
            Patch = $patch
        }
    }
}

if ($tagObjects.Count -eq 0) {
    Write-Error "Nessun tag Liferay valido trovato."
}

$latest = $tagObjects |
    Sort-Object Year, Quarter, Patch |
    Select-Object -Last 1

$latestTag = $latest.Tag

if ($AllowedLine -ne "") {
    Write-Host "Filtro linea:     $AllowedLine"
}
else {
    Write-Host "Filtro linea:     nessuno"
}

Write-Host "Ultimo tag remoto rilevato: $latestTag"
Write-Host ""

if ($currentTag -eq $latestTag) {
    Write-Host "Nessun aggiornamento necessario."
    exit 0
}

Write-Host "Nuova versione disponibile:"
Write-Host "  Da: $currentTag"
Write-Host "  A:  $latestTag"
Write-Host ""

Write-Host "Prima di procedere e' consigliato avere un backup DB e dei volumi."
$confirm = Read-Host "Vuoi aggiornare Liferay alla versione target? Scrivi esattamente YES per confermare"

if ($confirm -ne "YES") {
    Write-Host "Aggiornamento annullato."
    exit 0
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupEnv = "$EnvFile.bak.$timestamp"

Copy-Item $EnvFile $backupEnv

$newImage = "${Repository}:${latestTag}"

$content = Get-Content $EnvFile
$content = $content | ForEach-Object {
    if ($_ -match "^LIFERAY_IMAGE=") {
        "LIFERAY_IMAGE=$newImage"
    }
    else {
        $_
    }
}

Set-Content -Path $EnvFile -Value $content -Encoding UTF8

Write-Host ""
Write-Host "Backup creato: $backupEnv"
Write-Host "Aggiornato:    $EnvFile"
Write-Host ""

.\scripts\render-portal-ext.ps1

$composeArgs = @()

foreach ($file in $ComposeFiles) {
    $composeArgs += "-f"
    $composeArgs += $file
}

docker compose @composeArgs --env-file $EnvFile pull $ServiceName
docker compose @composeArgs --env-file $EnvFile up -d --force-recreate $ServiceName

Write-Host ""
Write-Host "Aggiornamento completato."