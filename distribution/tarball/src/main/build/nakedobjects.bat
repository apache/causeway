@echo off

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is the expanded pathname of the current script under NT
set LOCAL_NOF_HOME=
if "%OS%"=="Windows_NT" set LOCAL_NOF_HOME=%~dp0
if "%OS%"=="WINNT" set LOCAL_NOF_HOME=%~dp0

set LOCALCLASSPATH="%LOCAL_NOF_HOME%classes";"%LOCAL_NOF_HOME%resources";
for %%i in ("lib\*.jar") do call "%LOCAL_NOF_HOME%lcp.bat" "%%i"
for %%i in ("..\..\lib\*.jar") do call "%LOCAL_NOF_HOME%lcp.bat" "%%i"

java -cp %LOCALCLASSPATH% org.apache.isis.runtime.Isis %*

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal 
