
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM A graph containing all paths of length <= 2, from juc.locks.Lock to java.lang.Enum, within rt.jar.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -pathsg "java\.util\.concurrent\.locks\.Lock" "java\.lang\.Enum" -maxsteps 2

pause
