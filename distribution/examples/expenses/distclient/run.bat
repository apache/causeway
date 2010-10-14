@echo off

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is the expanded pathname of the current script under NT
set LOCAL_NOF_HOME=
if "%OS%"=="Windows_NT" set LOCAL_NOF_HOME=%~dp0
if "%OS%"=="WINNT" set LOCAL_NOF_HOME=%~dp0

set LOCALCLASSPATH=
for %%i in ("%LOCAL_NOF_HOME%.\lib\*.jar") do call "%LOCAL_NOF_HOME%\lcp.bat" "%%i"


rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).

set PARAM=%1
if ""%1""=="""" goto runCommand
shift

:loop
if ""%1""=="""" goto runCommand
set PARAM=%PARAM% %1
shift
goto loop


:runCommand
mkdir logs
java -cp %LOCALCLASSPATH% org.apache.isis.runtime.Isis -t client -v dnd %PARAM%

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
