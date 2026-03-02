@echo off

set NAME=%1%
if "%NAME%"=="" (
    set /p NAME="Informe o nome da migracao: "
)

set FOLDER_POSTGRESQL=src\main\resources\db\migration\postgresql

FOR /F %%A IN ('WMIC OS GET LocalDateTime ^| FINDSTR \.') DO @SET B=%%A

set MIGRATION_POSTGRESQL=%FOLDER_POSTGRESQL%\V%B:~0,4%%B:~4,2%%B:~6,2%%B:~8,2%%B:~10,2%%B:~12,2%__%NAME%.sql

echo Criando %MIGRATION_POSTGRESQL%
copy /y NUL %MIGRATION_POSTGRESQL% > NUL