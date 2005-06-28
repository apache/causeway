@echo off

REM This batch file is not intended for general use.

javah org.apache.log4j.nt.NTEventLogAppender
javah org.apache.log4j.Priority

MC EventLogCategories.mc

RC -r -fo EventLogCategories.res EventLogCategories.rc

SET JDK=c:\java\jdk1.1.7B\
rem SET JDK=c:\java\jdk1.3\

@echo "Compiling"
CL /nologo  /I %JDK%\include /I %JDK%\include\win32 /MD /W3 /GX /O2 /FD /c /D "NDEBUG" -DWINVER=0x400  -D_DLL -DWIN32 /D "_WINDOWS"  nteventlog.cpp

REM 

@echo
@echo "Linking"
LINK /subsystem:windows /INCREMENTAL:NO /dll /out:"NTEventLogAppender.dll" nteventlog.OBJ advapi32.lib EventLogCategories.res
