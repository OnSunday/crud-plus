# ----------------------------------
#  通过指定数量的优化能执行
#  -optimizationpasses n
# ----------------------------------
-optimizationpasses 3

# ----------------------------------
#   混淆时不会产生形形色色的类名
#   -dontusemixedcaseclassnames
# ----------------------------------
-dontusemixedcaseclassnames
# ----------------------------------
#      指定不去忽略非公共的库类
#  -dontskipnonpubliclibraryclasses
# ----------------------------------
#-dontskipnonpubliclibraryclasses

# ----------------------------------
#       不预校验
#    -dontpreverify
# ----------------------------------
# -dontpreverify

# ----------------------------------
#      输出生成信息
#       -verbose
# ----------------------------------
-verbose

#混淆时应用侵入式重载
-overloadaggressively

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification
#确定统一的混淆类的成员名称来增加混淆
-useuniqueclassmembernames

-ignorewarnings
-dontshrink
-dontoptimize
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

-keepattributes Signature
-keepattributes SourceFile,LineNumberTable,*Annotation*
-keepattributes **


## ################ Custom Protection ######################
#这里添加你不需要混淆的类
#-keep  class com.zsoftware.common.cache.** {*;}

# --------- 保护类及其成员名 ------------
-keepnames class com.jfeat.am.common.crud.*
-keepnames class com.jfeat.am.common.crud.impl.*




## ################ End Custom Protection ######################

##----------------------- public protection ------------##
#-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
}
# 保留public的方法
-keepclassmembers class * {
	public <methods>;
}
# 保留static的成员
-keepclassmembers class * {
    public static <fields>;
}
# 保留native方法的类名和方法名
-keepclasseswithmembernames class * {
    native <methods>;
}
# If you're using custom Eception
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.RuntimeException
# 保留所有接口
-keep public interface * { *; }
# 保留所有枚举及成员
-keep public enum * { <fields>; }

# as per official recommendation: https://github.com/evant/gradle-retrolambda#proguard
-dontwarn java.lang.invoke.*

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
#To use Single instead of Observable in Retrofit interface
# In order to provide the most meaningful crash reports
-keepattributes SourceFile,LineNumberTable

-keepnames class rx.Single

-dontwarn javax.annotation.**

# Findbugs jsr305
-dontwarn javax.annotation.**

# If you do not use RxJava:
-dontwarn rx.**

