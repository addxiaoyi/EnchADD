Set-Location "D:\qwq\项目\Enchantio-master\mod"
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
$env:GRADLE_USER_HOME="D:\.gradle"
& "D:\gradle-tmp\gradle-9.7.1\bin\gradle.bat" -p . ":fabric-1.21.6:generateEnchADDResources" --console=plain --no-daemon 2>&1 | Out-File "D:\qwq\项目\Enchantio-master\mod\synchk.log" -Encoding UTF8
