
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM Classes SCCs in java.awt.*.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -regex "java\.awt\..*" -sccs

pause
