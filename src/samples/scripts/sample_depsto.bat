
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM Classes of java.math.* that directly depend on java.lang.ArithmeticException.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -depsto "java\.lang\.ArithmeticException" -from "java\.math\..*" -maxsteps 1

pause
