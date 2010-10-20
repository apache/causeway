@echo off

rem
rem update DEPLOYMENT_FLAGS to customize the way that Naked Objects is run for a particular
rem deployment.  For example, to run as a client in client/server mode, use:
rem 
rem DEPLOYMENT_FLAGS=--type client --connector encoding-sockets
rem
rem Consult the Naked Objects documentation for the various options available.
rem

set DEPLOYMENT_FLAGS=


@setlocal

rem %~dp0 is the expanded pathname of the current script under NT
cd %~dp0

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).

set ARGS=%1
if ""%1""=="""" goto runCommand
shift

:loop
if ""%1""=="""" goto runCommand
set ARGS=%ARGS% %1
shift
goto loop

:runCommand
java -jar simple.jar %DEPLOYMENT_FLAGS% %ARGS%

@endlocal
