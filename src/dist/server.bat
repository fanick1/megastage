set JAVA_HOME="%~dp0jre7"
%JAVA_HOME%\bin\javaw.exe -cp lib/*:Megastage.jar org.megastage.server.Main --config world.xml
