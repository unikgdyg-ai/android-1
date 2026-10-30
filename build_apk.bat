@echo off
chcp 65001 >nul
if exist gradlew.bat (
    call gradlew.bat testDebugUnitTest assembleDebug
) else (
    gradle testDebugUnitTest assembleDebug
)
echo APK built at: app\build\outputs\apk\debug\app-debug.apk
pause
