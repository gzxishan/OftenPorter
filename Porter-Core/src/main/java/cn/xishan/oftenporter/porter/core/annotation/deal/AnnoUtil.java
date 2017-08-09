package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class AnnoUtil {


    public static String tied(_UnNece unNece, Field field, boolean enableDefaultValue) {
        String name = unNece.getValue();
        if (WPTool.isEmpty(name)) {
            if (!enableDefaultValue) {
                throw new InitException("default value is not enable for " + unNece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
    }

    public static String tied(_Nece nece, Field field, boolean enableDefaultValue) {
        String name = nece.getValue();
        if (WPTool.isEmpty(name)) {
            if (!enableDefaultValue) {
                throw new InitException("default value is not enable for " + nece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
    }


    static PortMethod[] methods(PortMethod classMethod, PortIn funPorterIn) {
        PortMethod[] portMethods = funPorterIn.methods();
        if (portMethods.length == 0) {
            portMethods = new PortMethod[]{funPorterIn.method()};
        }
        for (int i = 0; i < portMethods.length; i++) {
            PortMethod method = portMethods[i];
            if (classMethod == PortMethod.DEFAULT && method == PortMethod.DEFAULT) {
                method = PortMethod.GET;
            } else if (method == PortMethod.DEFAULT) {
                method = classMethod;
            }
            portMethods[i] = method;
        }
        return portMethods;
    }

    /**
     * 获取注解，若在当前函数中没有找到且此注解具有继承性则会尝试从父类中的(public)函数中查找。
     *
     * @param method
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        T t = getAnnotation(method, annotationClass, annotationClass.isAnnotationPresent(Inherited.class));
        return t;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        T t = clazz.getAnnotation(annotationClass);
        return t;
    }

    private static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass, boolean seekSuper) {
        T t = method.getAnnotation(annotationClass);
        if (t == null && seekSuper) {
            Class<?> clazz = method.getDeclaringClass().getSuperclass();
            if (clazz != null) {
                try {
                    method = clazz.getMethod(method.getName(), method.getParameterTypes());
                    t = getAnnotation(method, annotationClass, true);
                } catch (NoSuchMethodException e) {

                }
            }
        }
        return t;
    }
}
