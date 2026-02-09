@echo off
cd /d "c:\Users\l\Desktop\enadd\docs"
start /B node server8888.js > server.log 2>&1
timeout /t 3 /nobreak
echo Server started
