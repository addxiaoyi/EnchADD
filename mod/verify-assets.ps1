Add-Type -AssemblyName System.IO.Compression.FileSystem
$out = "D:\qwq\项目\Enchantio-master\release-assets\v2.1.17"
foreach ($m in @('fabric-1.21.2','fabric-1.21.6')) {
  $jar = Join-Path $out "enchadd-$m-2.1.17.jar"
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
  $e = $zip.Entries | Where-Object { $_.FullName -eq "fabric.mod.json" }
  $sr = New-Object System.IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close(); $zip.Dispose()
  $mc = ([regex]::Match($t, '"minecraft": "([^"]+)"')).Groups[1].Value
  "$m minecraft depends: $mc"
}
$jar = Join-Path $out "enchadd-forge-1.21.3-2.1.17.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
$e = $zip.Entries | Where-Object { $_.FullName -eq "META-INF/mods.toml" }
$sr = New-Object System.IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close(); $zip.Dispose()
$mc = ([regex]::Match($t, "versionRange=""\[1\.21\.\d,1\.21\.\d\)""")).Value
"forge-1.21.3 mc range: $mc"
