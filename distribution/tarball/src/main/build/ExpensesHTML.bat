@echo off

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is the expanded pathname of the current script under NT
set LOCAL_NOF_HOME=
if "%OS%"=="Windows_NT" set LOCAL_NOF_HOME=%~dp0
if "%OS%"=="WINNT" set LOCAL_NOF_HOME=%~dp0

call "%LOCAL_NOF_HOME%isis.bat" -v html

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal 




