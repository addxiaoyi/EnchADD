param([string]$A, [string]$B)
$root = "D:\qwq\项目\Enchantio-master\mod"
$aFiles = Get-ChildItem "$root\$A" -Recurse -Filter "*.java"
$diffList = @()
foreach ($f in $aFiles) {
  $rel = $f.FullName.Substring("$root\$A".Length).TrimStart("\")
  $other = Join-Path "$root\$B" $rel
  if (-not (Test-Path $other)) { $diffList += "MISSING: $rel"; continue }
  $ta = ([System.IO.File]::ReadAllText($f.FullName) -replace "`r`n", "`n")
  $tb = ([System.IO.File]::ReadAllText($other) -replace "`r`n", "`n")
  if ($ta -ne $tb) { $diffList += $rel }
}
"semantic diffs: $($diffList.Count)"
$diffList | Select-Object -First 25
