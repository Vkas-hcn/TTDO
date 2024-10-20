# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# ----------------------------------------------------------------------------
# 混淆的压缩比例，0-7
-optimizationpasses 5
# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# 指定混淆是采用的算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# 指定外部模糊字典 proguard-chinese.txt 改为混淆文件名，下同
-obfuscationdictionary proguard-o0O.txt
# 指定class模糊字典
-classobfuscationdictionary proguard-o0O.txt
# 指定package模糊字典
-packageobfuscationdictionary proguard-o0O.txt


-keepattributes *Annotation*
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-keep public class android.webkit.WebView
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient
-keep class com.pink.hami.melon.dual.option.bean.ScreenMetrics { *; }
-keep class com.pink.hami.melon.dual.option.bean.SmileAdBean { *; }
-keep class com.pink.hami.melon.dual.option.bean.SmileFlowBean { *; }
-keep class com.pink.hami.melon.dual.option.bean.SmileRefBean { *; }
-keep class com.pink.hami.melon.dual.option.bean.VpnServiceBean { *; }
-keep class com.pink.hami.melon.dual.option.bean.OnlineBean { *; }
-keep class com.pink.hami.melon.dual.option.bean.Data { *; }