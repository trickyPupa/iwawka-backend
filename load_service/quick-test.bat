@echo off

echo ====================================
echo Iwawka Quick Load Test
echo ====================================
echo.

echo Запуск быстрого теста (30 сек, 10 RPS)...
echo.

set STAGE_START_RPS=10
set STAGE_STEP_RPS=5
set STAGE_STEPS=2
set MAX_VUS=20
set STAGE_DURATION=30s

docker-compose -f docker-compose.monitoring.yml run --rm k6-load-test

echo.
echo Тест завершен!
echo.
pause
