
SET JAVA_HOME=C:\Program Files (x86)\Java\jdk1.5.0_22
PATH="%JAVA_HOME%\bin"

REM Going into the directory of this script.
cd "%~dp0"

REM Packages depending on java.net, included, with their predecessors (as successors), only considering API dependencies, in dot format, step by step, within rt.jar and java.*.
java -cp ../../../dist/jadecy.jar net.jadecy.cmd.JadecyMain "%JAVA_HOME%\jre\lib\rt.jar" -regex "java\..*" -packages -gdepsto "java\.net" -incl -steps -dotformat -apionly

pause
