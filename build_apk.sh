#!/usr/bin/env bash
set -e
echo "===================================================="
echo "  ساخت خودکار فایل نصبی اندروید «خرجیار» (KharjYar) "
echo "===================================================="
if command -v ./gradlew &> /dev/null; then
    ./gradlew testDebugUnitTest assembleDebug
else
    gradle testDebugUnitTest assembleDebug
fi
echo "✅ فایل APK در مسیر app/build/outputs/apk/debug/app-debug.apk ساخته شد."
