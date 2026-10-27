Set-Location "D:\qwq\项目\Enchantio-master\mod\forge-1.21.6\prod-run"
$env:_JAVA_OPTIONS = "-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=10808 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=10808"
& "C:\Program Files\Java\jdk-25.0.3\bin\java.exe" "@user_jvm_args.txt" "@libraries/net/minecraftforge/forge/1.21.6-56.0.9/win_args.txt" "nogui" 2>&1 | Out-File "D:\qwq\项目\Enchantio-master\mod\server-f126-prod.log" -Encoding UTF8
