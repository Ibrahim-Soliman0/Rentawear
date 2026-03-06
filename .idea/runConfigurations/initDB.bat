@echo off
setlocal

REM ==== SCRIPT DIRECTORY ====
set SCRIPT_DIR=%~dp0

REM ==== DROP & CREATE DATABASE ====
"%MYSQL_BIN%" -u %DB_USER% -p%DB_PASS% -e "DROP DATABASE IF EXISTS %DB_NAME%; CREATE DATABASE %DB_NAME%;"

REM ==== IMPORT SCHEMA ====
"%MYSQL_BIN%" -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SCRIPT_DIR%schema.sql"

REM ==== IMPORT DUMMY DATA ====
"%MYSQL_BIN%" -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SCRIPT_DIR%dummyData.sql"

echo ✅ Database initialized successfully