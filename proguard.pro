#禁用:bytecode优化；优化被开启后，成员函数的访问类型会可能变化。
-dontoptimize
#禁用:缩减没有使用的代码
-dontshrink

#禁用:预验证
#-dontpreverify#会导致java.lang.VerifyError: Expecting a stackmap frame

#保留目录元素，这样classloader可以正常扫描。
-keepdirectories

-ignorewarnings

-dontwarn java*
-dontwarn cn.oftenporter.oftendb*

#本地方法相关信息保留
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

#slf4j
-keep class org.slf4j.impl.**{*;}
#logback
-keep class ch.qos.logback.**{*;}

#-ignorewarnings

#枚举
-keep enum **{*;}
#注解
-keep interface * extends java.lang.annotation.Annotation { *; }
#保留注解
-keepparameternames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

######Porter-Core########
#-keep  class cn.oftenporter.porter**
-keep public interface cn.oftenporter**{public protected *;}#保留所有接口
-keep public class cn.oftenporter**$*{public protected *;}#保留内部类
-keep public class cn.oftenporter**PorterProcessor{public protected *;}
-keep public class cn.oftenporter.porter.core.util**{public protected *;}
-keep public class cn.oftenporter**Util{public protected *;}
-keep public class cn.oftenporter**Adapter{public protected *;}
-keep public class cn.oftenporter**Exception{public protected *;}
-keep public class cn.oftenporter.porter**WObject{public protected *;}
-keep public class cn.oftenporter.porter**InNames{public protected *;}
-keep public class cn.oftenporter.porter**PRequest{public protected *;}
-keep public class cn.oftenporter.porter**PResponse{public protected *;}
-keep public class cn.oftenporter.porter**PName{public protected *;}
-keep public class cn.oftenporter.porter**PPath{public protected *;}
-keep public class cn.oftenporter.porter**JResponse{public protected *;}
-keep public class cn.oftenporter.porter**ParamSourceHandleManager{public protected *;}
-keep public class cn.oftenporter.porter**PreRequest{public protected *;}
-keep public class cn.oftenporter.porter**TypeTo{public protected *;}
-keep public class cn.oftenporter.porter**LocalMain{public protected *;}
-keep public class cn.oftenporter.porter.simple**{public protected *;}
-keep public class cn.oftenporter.porter.core.init**Porter*{public protected *;}
-keep public class cn.oftenporter.porter.core.init**SeekPackages{public protected *;}

#json
#-keep class com.alibaba.fastjson**{public *;}

