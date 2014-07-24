echo Fanfare for Space Kevin MacLeod (incompetech.com)
echo All This Kevin MacLeod (incompetech.com)

set JAVA_HOME=%~dp0jre7
start "Megastage Server" server.bat

timeout 5 /nobreak
start "Megastage Client" client.bat
