# قوانین ProGuard / R8 برای حفظ مدل‌های سریالایزیشن و دیتابیس Room
-keepattributes *Annotation*,InnerClasses,Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class ir.kharjyar.domain.model.** { *; }
-keep class ir.kharjyar.data.local.entity.** { *; }
