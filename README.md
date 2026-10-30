# خرجیار (KharjYar) — سامانه هوشمند مدیریت مالی شخصی و کسب‌وکار

اپلیکیشن اندروید مدرن، امن و چندماژوله برای مدیریت دقیق مخارج، درآمدها، بودجه‌بندی، اهداف پس‌انداز، بدهی/طلب و حسابداری کسب‌وکار (فاکتور، چک و جریان نقدی) با پشتیبانی کامل از تقویم شمسی (جلالی) و میلادی.

## ۱. معماری و استک فنی (Tech Stack)
- **زبان**: Kotlin 2.0.21 (Coroutines + Flow + StateFlow)
- **رابط کاربری**: Jetpack Compose (Material 3 + Material You Dynamic Color + Custom Canvas)
- **معماری**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **ماژول‌بندی**:
  - `:core`: ابزارهای مشترک، مدیریت خطا (`Result`)، مدل پول (`Money`)، ماشین‌حساب داخلی، مبدل تاریخ جلالی/میلادی، تنظیمات `DataStore`، تم و سیستم طراحی (`Design System`).
  - `:domain`: مدل‌های دامنه، رابط‌های Repository و UseCaseهای خالص بدون وابستگی به فریم‌ورک اندروید.
  - `:data`: پیاده‌سازی دیتابیس `Room` (با `withTransaction`، کلیدهای خارجی، ایندکس‌ها و Migration)، `DataStore` و Repositoryها.
  - `:presentation`: صفحات Compose، `ViewModel`ها، مدیریت وضعیت (`UiState` / `UiEvent` / `UiEffect`) و گراف ناوبری (`Navigation Compose`).
  - `:app`: نقطه ورود برنامه، پیکربندی `Hilt`، `WorkManager`، ویجت `Glance` و میانبرها (`App Shortcuts`).
- **تزریق وابستگی**: Dagger Hilt (`2.52`)
- **ذخیره‌سازی**: Room (`2.6.1`) + Jetpack Preferences DataStore (`1.1.1`)
- **حداقل نسخه اندروید**: `minSdk = 26` (اندروید ۸.۰) | `targetSdk = 35` (اندروید ۱۵)

## ۲. اصول دقت مالی و تصمیمات طراحی (فروض منطقی)
1. **عدم استفاده مطلق از `Float` و `Double` برای پول**: تمامی مبالغ در سراسر اپ در قالب کلاس ارزش (`@JvmInline value class Money(val minorUnits: Long)`) بر حسب کوچک‌ترین واحد ارزی (ریال) ذخیره و پردازش می‌شوند. در صورت انتخاب واحد نمایش «تومان»، تبدیل صرفاً در لایه نمایش (`MoneyFormatter`) انجام می‌شود و دقت ریالی در دیتابیس حفظ می‌گردد.
2. **محاسبات کسری و مالیاتی**: برای محاسبه درصدها، مالیات بر ارزش افزوده (VAT) و تقسیم تراکنش‌ها (`Split Transaction`) از `BigDecimal` با گرد کردن استاندارد بانکی (`RoundingMode.HALF_UP`) استفاده شده و باقیمانده تقسیم (Remainder) به آخرین سهم اضافه می‌شود تا جمع کل سهام دقیقاً با مبلغ تراکنش برابر بماند.
3. **ذخیره‌سازی تاریخ و زمان**: تمام زمان‌ها به صورت `UTC Epoch Millis (Long)` ذخیره می‌شوند و در لایه UI بر اساس انتخاب کاربر به تقویم **شمسی (جلالی)** یا **میلادی** با شروع هفته از **شنبه** تبدیل می‌گردند.
4. **تفکیک کامل فضاهای کاری (Workspaces)**: هر فضای کاری (`Personal` یا پروفایل‌های متعدد `Business`) دارای شناسه یکتا (`workspaceId`) است و تم رنگی برنامه به صورت انیمیشنی هنگام سوییچ بین فضای شخصی (زمردی/فیروزه‌ای) و کسب‌وکار (لاجوردی/طلایی) تغییر می‌کند.

## ۳. ساختار درختی پروژه
```text
KharjYar/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/ir/kharjyar/app/
│       │   ├── KharjYarApplication.kt
│       │   └── MainActivity.kt
│       └── res/
│           ├── values/strings.xml
│           ├── values-en/strings.xml
│           └── xml/shortcuts.xml
├── core/
│   ├── build.gradle.kts
│   └── src/main/java/ir/kharjyar/core/
│       ├── common/Result.kt
│       ├── money/Money.kt
│       ├── money/ExpressionEvaluator.kt
│       ├── date/JalaliDateConverter.kt
│       ├── preferences/AppPreferencesDataStore.kt
│       └── designsystem/
│           ├── theme/Color.kt
│           ├── theme/Type.kt
│           ├── theme/Shape.kt
│           ├── theme/Theme.kt
│           └── components/DesignSystemComponents.kt
├── domain/
│   ├── build.gradle.kts
│   └── src/main/java/ir/kharjyar/domain/model/WorkspaceModels.kt
├── data/
│   └── build.gradle.kts
└── presentation/
    ├── build.gradle.kts
    └── src/main/java/ir/kharjyar/presentation/
        ├── navigation/
        │   ├── Screen.kt
        │   ├── FloatingBottomNavBar.kt
        │   └── KharjYarNavHost.kt
        ├── main/
        │   ├── MainContract.kt
        │   ├── MainViewModel.kt
        │   └── MainScaffold.kt
        └── onboarding/
            └── OnboardingScreen.kt
```

## ۴. راهنمای اجرا
1. پروژه را در **Android Studio Ladybug (2024.2.1)** یا بالاتر باز کنید.
2. فونت‌های خانواده `Vazirmatn` (`vazirmatn_regular.ttf`, `vazirmatn_medium.ttf`, `vazirmatn_bold.ttf`) را در مسیر `core/src/main/res/font/` قرار دهید (سیستم فونت دارای Fallback خودکار به `SansSerif` است تا بدون فایل فونت نیز کامپایل و اجرا شود).
3. دستور `./gradlew assembleDebug` یا `./gradlew testDebugUnitTest` را اجرا نمایید.
