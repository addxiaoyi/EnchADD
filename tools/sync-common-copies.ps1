# Syncs per-generation common source copies from the shared source tree.
# Usage: pwsh tools/sync-common-copies.ps1
# Regenerates common-1.21.1/, common-g2/, common-g3/ inside each module
# so the static copies never drift from the shared source.

$ErrorActionPreference = "Stop"
$root = "$PSScriptRoot\..\mod"
$shared = "$root\common\src\main\java"

# Generation transforms applied to the shared source (which targets 1.21.11/26.3 naming).
$transforms = @{
    "legacy" = {
        param([string]$t)
        $t = $t -replace "import net\.minecraft\.resources\.Identifier;", "import net.minecraft.resources.ResourceLocation;"
        $t = $t -replace "\bIdentifier\b", "ResourceLocation"
        $t = $t.Replace("MobEffectInstance adjusted = effect.withScaledDuration((float) (1.0 - reduction));",
            "MobEffectInstance adjusted = new MobEffectInstance(effect.getEffect(), Math.max(1, (int) (effect.getDuration() * (1.0 - reduction))), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());")
        $t = $t -replace "ResolvableProfile\.createResolved\((\w+\.getGameProfile\(\))\)", "new ResolvableProfile(`$1)"
        return $t
    }
}

# Target copy directories and their generation
$targets = @(
    @{ dir = "$root\fabric-1.21.1\common-1.21.1"; gen = "legacy" },
    @{ dir = "$root\fabric-1.21.2\common-g2";     gen = "legacy" },
    @{ dir = "$root\forge-1.21.3\common-g2";      gen = "legacy" },
    @{ dir = "$root\neoforge-1.21.3\common-g2";   gen = "legacy" },
    @{ dir = "$root\fabric-1.21.6\common-g3";     gen = "legacy" },
    @{ dir = "$root\neoforge-1.21.8\common-g3";   gen = "legacy" }
)

foreach ($target in $targets) {
    $destRoot = $target.dir
    $gen = $target.gen
    $transform = $transforms[$gen]
    $changed = 0
    $sharedFiles = Get-ChildItem $shared -Recurse -Filter "*.java"
    foreach ($sf in $sharedFiles) {
        $rel = $sf.FullName.Substring($shared.Length).TrimStart("\")
        $dest = Join-Path $destRoot $rel
        $destDir = Split-Path $dest -Parent
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
        $converted = [System.Text.Encoding]::UTF8.GetString([System.IO.File]::ReadAllBytes($sf.FullName))
        $converted = & $transform $converted
        $existing = if (Test-Path $dest) { [System.IO.File]::ReadAllText($dest) } else { "" }
        if (($converted -replace "`r`n", "`n") -ne ($existing -replace "`r`n", "`n")) {
            [System.IO.File]::WriteAllText($dest, $converted, (New-Object System.Text.UTF8Encoding($false)))
            $changed++
        }
    }
    Write-Output "$destRoot : $changed files updated (of $($sharedFiles.Count))"
}
