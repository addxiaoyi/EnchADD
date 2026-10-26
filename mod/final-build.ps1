Set-Location "D:\qwq\项目\Enchantio-master\mod"
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
$env:GRADLE_USER_HOME="D:\.gradle"
$env:GRADLE_OPTS='"-Dhttp.proxyHost=127.0.0.1" "-Dhttp.proxyPort=10808" "-Dhttps.proxyHost=127.0.0.1" "-Dhttps.proxyPort=10808"'
& "D:\gradle-tmp\gradle-9.7.1\bin\gradle.bat" -p . ":fabric-1.21.1:build" ":fabric-1.21.2:build" ":fabric-1.21.6:build" ":fabric-1.21.11:build" ":fabric-26.3:build" ":forge-1.21.1:build" ":forge-1.21.3:build" ":forge-1.21.11:build" ":forge-26.3:build" ":neoforge-1.21.1:build" ":neoforge-1.21.3:build" ":neoforge-1.21.8:build" ":neoforge-1.21.11:build" ":neoforge-26.3:build" --console=plain --no-daemon 2>&1 | Out-File "D:\qwq\项目\Enchantio-master\mod\final-build.log" -Encoding UTF8
