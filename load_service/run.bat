@echo off

echo ====================================
echo Iwawka Load Testing Suite
echo ====================================
echo.

echo [1/4] Проверка основной инфраструктуры...
docker ps | findstr "iwawka-chat" >nul 2>&1
if errorlevel 1 (
    echo ОШИБКА: Основной docker-compose не запущен!
    echo Сначала запустите: docker-compose up -d
    exit /b 1
)
echo ✓ Основная инфраструктура запущена

echo.
echo [2/4] Запуск Prometheus и Grafana...
docker-compose -f docker-compose.monitoring.yml up -d prometheus grafana
if errorlevel 1 (
    echo ОШИБКА: Не удалось запустить мониторинг
    exit /b 1
)
echo ✓ Мониторинг запущен

echo.
echo [3/4] Ожидание готовности сервисов (10 сек)...
timeout /t 10 /nobreak >nul
echo ✓ Сервисы готовы

echo.
echo [4/4] Запуск нагрузочного теста...
echo Параметры:
echo   - Начальная нагрузка: %STAGE_START_RPS% RPS (по умолчанию 50)
echo   - Шаг увеличения: %STAGE_STEP_RPS% RPS (по умолчанию 50)
echo   - Количество этапов: %STAGE_STEPS% (по умолчанию 5)
echo   - Длительность этапа: %STAGE_DURATION% (по умолчанию 1m)
echo   - Максимум VU: %MAX_VUS% (по умолчанию 200)
echo.

docker-compose -f docker-compose.monitoring.yml run --rm k6-load-test

echo.
echo ====================================
echo Тест завершен!
echo ====================================
echo.
echo Результаты доступны:
echo   - Prometheus: http://localhost:9090
echo   - Grafana:    http://localhost:3000 (admin/admin)
echo.
echo Для остановки мониторинга:
echo   docker-compose -f docker-compose.monitoring.yml down
echo.

pause

