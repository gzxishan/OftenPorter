package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnotationSearch
{

    /**
     * 搜索指定的类中包含指定注解的字段(public)
     *
     * @param forEach          对于每个搜索到的字段的处理
     * @param forSearch        在此类中搜索
     * @param exceptFieldsName 排除的字段名
     * @param annotationClass  注解
     * @throws Exception
     */
    public static void searchPublicFields(MyConsumer<Field, Exception> forEach, Class<?> forSearch, String[] exceptFieldsName,
            Class<? extends Annotation>... annotationClass) throws Exception
    {
        Field[] fields = forSearch.getFields();
        boolean find;
        for (Field field : fields)
        {

            find = true;
            for (Class<? extends Annotation> annotation : annotationClass)
            {
                if (!field.isAnnotationPresent(annotation))
                {
                    find = false;
                    break;
                }
            }
            if (find && !except(exceptFieldsName, field.getName()))
            {
                forEach.accept(field);
            }
        }
    }

    private static boolean except(String[] exceptFieldsName, String name)
    {
        boolean except = false;
        if (exceptFieldsName != null)
        {
            for (String string : exceptFieldsName)
            {
                if (string.equals(name))
                {
                    except = true;
                    break;
                }
            }
        }
        return except;
    }

//    /**
//     * 寻找一个含有指定注解的字段的值.
//     *
//     * @param source 被搜索的对象
//     * @param annotation 要搜索的注解
//     * @return 若找到，则返回对应的值；否则返回null。
//     */
//    public static Object getOneFieldValue(Object source, Class<? extends Annotation> annotation)
//    {
//        try
//        {
//            Field[] fields = source.getClass().getDeclaredFields();
//            for (Field field : fields)
//            {
//                if (field.isAnnotationPresent(annotation))
//                {
//                    field.setAccessible(true);
//                    return field.get(source);
//                }
//            }
//        } catch (Exception e)
//        {
//
//        }
//
//        return null;
//    }

}
