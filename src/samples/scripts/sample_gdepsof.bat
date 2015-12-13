
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM Packages java.lang depends on, step by step, with their successors starting with java.util., only considering API dependencies, without causes, within rt.jar.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -packages -gdepsof "java\.lang" -steps -into "java\.util\..*" -apionly -nocauses

pause
