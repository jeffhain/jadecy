
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM API dependencies in java.lang.*, added by java.awt.Component over java.lang.Object, within rt.jar, step by step.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -depsof "java.awt.Component" -minusof "java.lang.Object" -into "java.lang.*" -apionly -steps

pause
