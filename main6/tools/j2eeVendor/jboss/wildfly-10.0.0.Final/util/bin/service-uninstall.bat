@echo off

rem echo.
cd %~dp0..\..\server\bin\service
rem echo Stop GlobalSight service if it is running...
net stop "GlobalSight Service"
rem echo Remove old GlobalSight service using windows command...
sc delete "GlobalSight"
rem echo Uninstall GlobalSight service using WildFly command...
service.bat uninstall