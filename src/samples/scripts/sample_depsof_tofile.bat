
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM Dependencies of java.lang.Object, itself included, within rt.jar, step by step, without stats, into a file.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -depsof "java\.lang\.Object" -incl -steps -nostats -tofile "sample_output/object_deps.txt"

pause
