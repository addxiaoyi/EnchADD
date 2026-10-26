Set-Location "D:\qwq\项目\Enchantio-master\mod"
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
$env:GRADLE_USER_HOME="D:\.gradle"
$env:GRADLE_OPTS='"-Dhttp.proxyHost=127.0.0.1" "-Dhttp.proxyPort=10808" "-Dhttps.proxyHost=127.0.0.1" "-Dhttps.proxyPort=10808"'
& "D:\gradle-tmp\gradle-9.7.1\bin\gradle.bat" -p . ":fabric-1.21.2:build" --console=plain --no-daemon 2>&1 | Out-File "D:\qwq\项目\Enchantio-master\mod\vb2.log" -Encoding UTF8
