param(
    [string]$Version = "",
    [string]$AabPath = "",
    [string]$DesktopPath = ""
)

$ErrorActionPreference = "Stop"

function Resolve-Version {
    param([string]$ExplicitVersion)

    if ($ExplicitVersion.Trim().Length -gt 0) {
        return $ExplicitVersion.TrimStart("v")
    }

    $buildFile = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
    $versionLine = Select-String -Path $buildFile -Pattern 'versionName\s*=' | Select-Object -First 1
    if ($null -eq $versionLine -or $versionLine.Line -notmatch '"([^"]+)"') {
        throw "Could not resolve versionName from app/build.gradle.kts"
    }

    return $Matches[1]
}

function Resolve-DesktopPath {
    param([string]$ExplicitDesktopPath)

    $candidates = @()
    if ($ExplicitDesktopPath.Trim().Length -gt 0) {
        $candidates += $ExplicitDesktopPath
    }
    if ($env:OneDrive) {
        $candidates += (Join-Path $env:OneDrive "바탕 화면")
        $candidates += (Join-Path $env:OneDrive "Desktop")
    }
    $shellDesktop = [Environment]::GetFolderPath("Desktop")
    if ($shellDesktop) {
        $candidates += $shellDesktop
    }
    $candidates += (Join-Path $HOME "Desktop")

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate -PathType Container)) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    if ($shellDesktop) {
        New-Item -ItemType Directory -Force -Path $shellDesktop | Out-Null
        return (Resolve-Path -LiteralPath $shellDesktop).Path
    }

    throw "Could not resolve a Desktop path."
}

function Resolve-AabPath {
    param([string]$ExplicitAabPath)

    if ($ExplicitAabPath.Trim().Length -gt 0) {
        if (-not (Test-Path -LiteralPath $ExplicitAabPath -PathType Leaf)) {
            throw "AAB file not found: $ExplicitAabPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitAabPath).Path
    }

    $releaseBundleDir = Join-Path $PSScriptRoot "..\app\build\outputs\bundle\release"
    $aab = Get-ChildItem -Path $releaseBundleDir -Filter "*.aab" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $aab) {
        throw "Release AAB not found. Build it first: .\gradlew.bat bundleRelease"
    }

    return $aab.FullName
}

$resolvedVersion = Resolve-Version -ExplicitVersion $Version
$desktop = Resolve-DesktopPath -ExplicitDesktopPath $DesktopPath
$sourceAab = Resolve-AabPath -ExplicitAabPath $AabPath
$notesPath = Join-Path $PSScriptRoot "..\play_store\release_notes\v$resolvedVersion.txt"

if (-not (Test-Path -LiteralPath $notesPath -PathType Leaf)) {
    throw "Play Store release notes not found: $notesPath"
}

$targetAab = Join-Path $desktop "zephyr-sky-v$resolvedVersion.aab"
$targetNotes = Join-Path $desktop "zephyr-sky-v$resolvedVersion-release-notes.txt"

Copy-Item -LiteralPath $sourceAab -Destination $targetAab -Force
Copy-Item -LiteralPath $notesPath -Destination $targetNotes -Force

Write-Host "Exported Play Store files:"
Write-Host "- $targetAab"
Write-Host "- $targetNotes"
