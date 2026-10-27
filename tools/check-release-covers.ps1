Add-Type -AssemblyName System.IO.Compression.FileSystem
$out = "D:\qwq\项目\Enchantio-master\release-assets\v2.1.17"
foreach ($jar in (Get-ChildItem $out -Filter "enchadd-fabric-*.jar")) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  $e = $zip.Entries | Where-Object { $_.FullName -eq "fabric.mod.json" }
  $sr = New-Object System.IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close()
  $mc = ([regex]::Match($t, '"minecraft": "([^"]+)"')).Groups[1].Value
  "$($jar.Name) : minecraft=$mc"
  $zip.Dispose()
}
foreach ($jar in (Get-ChildItem $out -Filter "enchadd-forge-*.jar")) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  $e = $zip.Entries | Where-Object { $_.FullName -eq "META-INF/mods.toml" }
  $sr = New-Object System.IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close()
  $mc = ([regex]::Match($t, 'modId="minecraft"[^[]*versionRange="([^"]+)"')).Groups[1].Value
  "$($jar.Name) : minecraft=$mc"
  $zip.Dispose()
}
foreach ($jar in (Get-ChildItem $out -Filter "enchadd-neoforge-*.jar")) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  $e = $zip.Entries | Where-Object { $_.FullName -eq "META-INF/neoforge.mods.toml" }
  $sr = New-Object System.IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close()
  $mc = ([regex]::Match($t, 'modId="minecraft"[^[]*versionRange="([^"]+)"')).Groups[1].Value
  "$($jar.Name) : minecraft=$mc"
  $zip.Dispose()
}
