package cn.xishan.oftenporter.porter.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class LogUtil
{
    /**
     * 得到当前代码执行的位置。
     *
     * @return
     */
    public static String getCodePos()
    {
        return getCodePos(2);
    }

    /**
     * 得到代码的位置。
     *
     * @param n
     * @return
     */
    public static String getCodePos(int n)
    {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stacks[n+1];
        if(stackTraceElement.getClassName().equals(LogUtil.class.getName())){
            stackTraceElement=stacks[n+2];
        }
        return toString(stackTraceElement);
    }

    public static String toString(StackTraceElement stackTraceElement)
    {
        StringBuilder sb = new StringBuilder();
        String name = stackTraceElement.getClassName();
        name = name.substring(name.lastIndexOf('.') + 1);
        int index = name.indexOf('$');
        if (index > 0)
        {
            name = name.substring(0, index);
        }
        sb.append("at ").append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName())
                .append("(" + name + ".java:" + stackTraceElement.getLineNumber() + ")");
        return sb.toString();
    }


    /**
     * 得到方法和类名[methodName,className]
     *
     * @param n
     * @return
     */
    public static Object[] methodAndClass(int n)
    {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        StackTraceElement stackTraceElement = stacks[n];
        return new Object[]{stackTraceElement.getMethodName(), stackTraceElement.getClassName()};
    }

    /**
     * 打印当前位置
     */
    public static void printPosLn(Object... objects)
    {
        System.out.println(getCodePos(2));
        for (Object object : objects)
        {
            System.out.print(object);
        }
        System.out.println();
    }

    public static void printPos(Object... objects)
    {
        System.out.print(getCodePos(2));
        for (Object object : objects)
        {
            System.out.print(object);
        }
        System.out.println();
    }

    /**
     * 打印当前位置
     */
    public static void printErrPosLn(Object... objects)
    {
        System.err.println(getCodePos(2));
        for (Object object : objects)
        {
            System.err.print(object);
        }
        System.err.println();
    }

    public static void printErrPos(Object... objects)
    {
        System.err.print(getCodePos(2));
        for (Object object : objects)
        {
            System.err.print(object);
        }
        System.err.println();
    }

    /**
     * 打印当前位置
     * @param stack 0表示调用的地方，1表示上一个地方，依次类推。
     * @param objects
     */
    public static void printErrPosLnS(int stack, Object... objects)
    {
        System.err.println(getCodePos(stack + 2));
        for (Object object : objects)
        {
            System.err.print(object);
        }
        System.err.println();
    }

    /**
     * 见{@linkplain #printErrPosLnS(int, Object...)}
     * @param stack
     * @param objects
     */
    public static void printPosLnS(int stack, Object... objects)
    {
        System.out.println(getCodePos(stack + 2));
        for (Object object : objects)
        {
            System.out.print(object);
        }
        System.out.println();
    }


    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yy/MM/dd HH:mm");

    /**
     * 设置时间格式化对象。
     *
     * @param simpleDateFormat
     */
    public static void setSimpleDateFormat(SimpleDateFormat simpleDateFormat)
    {
        if (simpleDateFormat == null)
        {
            throw new NullPointerException();
        }
        LogUtil.simpleDateFormat = simpleDateFormat;
    }

    /**
     * 得到当前时间
     *
     * @return
     */
    public static String getTime()
    {
        return getTime(Calendar.getInstance().getTime());
    }


    /**
     * @param calendar
     * @return
     */
    public static String getTime(Calendar calendar)
    {
        return getTime(calendar.getTime());
    }

    /**
     * @param mills 毫秒数
     * @return
     */
    public static String getTime(long mills)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * @param date 日期对象
     * @return
     */
    public static String getTime(Date date)
    {
        return simpleDateFormat.format(date);
    }
}
