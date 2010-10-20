@echo off

@setlocal

rem %~dp0 is the expanded pathname of the current script under NT
cd %~dp0

java -jar restful.testapp.jar -t exploration -v html

@endlocal
