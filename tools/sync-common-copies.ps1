# Syncs per-generation common source copies from the shared source tree.
# Usage: pwsh tools/sync-common-copies.ps1
# Run after editing shared common sources (mod/common/src/main/java).

$ErrorActionPreference = "Stop"
$root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\mod"))
$shared = [System.IO.Path]::GetFullPath((Join-Path $root "common\src\main\java"))
$holdfastOverride = [System.IO.Path]::GetFullPath((Join-Path $root "fabric-1.21.1\common-1.21.1\net\enchadd\mod\common\HoldfastBehavior.java"))

$targets = @(
    (Join-Path $root "fabric-1.21.1\common-1.21.1"),
    (Join-Path $root "fabric-1.21.2\common-g2"),
    (Join-Path $root "forge-1.21.3\common-g2"),
    (Join-Path $root "neoforge-1.21.3\common-g2"),
    (Join-Path $root "fabric-1.21.6\common-g3"),
    (Join-Path $root "neoforge-1.21.8\common-g3")
)

$sharedFiles = Get-ChildItem $shared -Recurse -Filter "*.java"
foreach ($destRoot in $targets) {
    $changed = 0
    foreach ($sf in $sharedFiles) {
        $rel = [System.IO.Path]::GetRelativePath($shared, $sf.FullName)
        $dest = Join-Path $destRoot $rel
        $destDir = Split-Path $dest -Parent
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
        if ($rel -match "HoldfastBehavior" -and (Test-Path $holdfastOverride)) {
            $converted = [System.IO.File]::ReadAllText($holdfastOverride)
        } else {
            $converted = [System.Text.Encoding]::UTF8.GetString([System.IO.File]::ReadAllBytes($sf.FullName))
            # Identifier -> ResourceLocation (pre-1.21.11)
            $converted = $converted -replace "import net\.minecraft\.resources\.Identifier;", "import net.minecraft.resources.ResourceLocation;"
            $converted = $converted -replace "\bIdentifier\b", "ResourceLocation"
            # arrow subpackage -> flat projectile (pre-1.21.9)
            $converted = $converted -replace "net\.minecraft\.world\.entity\.projectile\.arrow\.", "net.minecraft.world.entity.projectile."
            # advancements.criterion -> critereon (pre-1.21.9)
            $converted = $converted -replace "import net\.minecraft\.advancements\.criterion\.", "import net.minecraft.advancements.critereon."
            $converted = $converted -replace "advancements\.criterion\.FishingRodHookedTrigger", "advancements.critereon.FishingRodHookedTrigger"
            $converted = $converted -replace "Lnet/minecraft/advancements/criterion/FishingRodHookedTrigger;", "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;"
            # MobEffects old names (pre-1.21.11 uses SPEED/RESISTANCE etc. for 1.21.5-1.21.10;
            # 1.21.1/1.21.2-1.21.4 use new names MOVEMENT_SPEED etc.)
            # G3 (1.21.6-1.21.10) keeps old names = no change needed for common-g3
            # G2/common-1.21.1 uses new names = apply transform
            if ($destRoot -match "common-1\.21\.1|common-g2") {
                $converted = $converted -replace "MobEffects\.SLOWNESS\b", "MobEffects.MOVEMENT_SLOWDOWN"
                $converted = $converted -replace "MobEffects\.MINING_FATIGUE\b", "MobEffects.DIG_SLOWDOWN"
                $converted = $converted -replace "MobEffects\.HASTE\b", "MobEffects.DIG_SPEED"
                $converted = $converted -replace "MobEffects\.RESISTANCE\b", "MobEffects.DAMAGE_RESISTANCE"
                $converted = $converted -replace "MobEffects\.JUMP_BOOST\b", "MobEffects.JUMP"
                $converted = $converted -replace "MobEffects\.STRENGTH\b", "MobEffects.DAMAGE_BOOST"
                $converted = $converted -replace "MobEffects\.NAUSEA\b", "MobEffects.CONFUSION"
                $converted = $converted -replace "MobEffects\.SPEED\b", "MobEffects.MOVEMENT_SPEED"
            }
            # hurtServer -> hurt (pre-1.21.2)
            $converted = $converted -replace "ResolvableProfile\.createResolved\((\w+\.getGameProfile\(\))\)", "new ResolvableProfile(`$1)"
            $converted = $converted -replace "\.getLastHurtByPlayer\(\)", ".getKillCredit()"
            if ($destRoot -match "common-1\.21\.1") {
                $converted = $converted -replace "\.hurtServer\(", ".hurt("
                $converted = $converted -replace "player\.teleport\(player\.findRespawnPositionAndUseSpawnBlock\(false, DimensionTransition\.DO_NOTHING\)\);", "player.changeDimension(player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING));"
                $converted = $converted -replace "import net\.minecraft\.world\.level\.portal\.TeleportTransition;", "import net.minecraft.world.level.portal.DimensionTransition;"
                $converted = $converted -replace "\bTeleportTransition\b", "DimensionTransition"
                $converted = $converted -replace "Player actualKiller = victim\.getLastHurtByPlayer\(\);", "Player actualKiller = victim.getKillCredit() instanceof Player p ? p : null;"
                $converted = $converted -replace "\.getLastHurtByPlayer\(\)", ".getKillCredit()"
                $converted = $converted -replace "\.recipeAccess\(\)", ".getRecipeManager()"
                $converted = $converted -replace "isOnCooldown\(rod\)", "isOnCooldown(rod.getItem())"
                $converted = $converted -replace "addCooldown\(rod, ", "addCooldown(rod.getItem(), "
                $converted = $converted -replace "isOnCooldown\(item\)", "isOnCooldown(item.getItem())"
                $converted = $converted -replace "addCooldown\(item, ", "addCooldown(item.getItem(), "
                $converted = $converted -replace "ResolvableProfile\.createResolved\((\w+\.getGameProfile\(\))\)", "new ResolvableProfile(`$1)"
            }
            # Lucidity withScaledDuration (1.21.6-1.21.10 and 1.21.1 lack it)
            if ($destRoot -match "common-1\.21\.1|common-g3") {
                $converted = $converted.Replace("MobEffectInstance adjusted = effect.withScaledDuration((float) (1.0 - reduction));",
                    "MobEffectInstance adjusted = new MobEffectInstance(effect.getEffect(), Math.max(1, (int) (effect.getDuration() * (1.0 - reduction))), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());")
            }
        }
        $existing = if (Test-Path $dest) { [System.IO.File]::ReadAllText($dest) } else { $null }
        if (($existing -replace "`r`n", "`n") -ne ($converted -replace "`r`n", "`n")) {
            [System.IO.File]::WriteAllText($dest, $converted, (New-Object System.Text.UTF8Encoding($false)))
            $changed++
        }
    }
    Write-Output "$($destRoot -replace [regex]::Escape($root), '') : $changed files updated (of $($sharedFiles.Count))"
}
