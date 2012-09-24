@ echo off
setlocal ENABLEDELAYEDEXPANSION
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_30
set path=C:\Program Files\Java\jdk1.6.0_30\bin
set WWS_HOME=.
set CLASSPATH=%WWS_HOME%\bin
set JVM_ARGS=-server -XX:+UseParallelGC  -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx1024M 
for /R .\lib %%A in (*.jar) do (
SET CLASSPATH=!CLASSPATH!;%%A
)
echo %CLASSPATH% > 1.txt
java %JVM_ARGS% code.google.dsf.test.StartServerTest



