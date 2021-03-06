package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class LogUtil
{

    public interface OnGetLoggerListener
    {
        Logger getLogger(String name);
    }

    private static final ThreadLocal<OnGetLoggerListener> LISTENER_THREAD_LOCAL = new ThreadLocal<>();

    private static boolean isDefaultLogger = true;
    private static OnGetLoggerListener defaultOnGetLoggerListener = name -> LoggerFactory.getLogger(name);

    public synchronized static void setDefaultOnGetLoggerListener(OnGetLoggerListener defaultOnGetLoggerListener)
    {
        LogUtil.defaultOnGetLoggerListener = defaultOnGetLoggerListener;
    }

    public static boolean isDefaultLogger()
    {
        return isDefaultLogger;
    }

    public static void setDefaultLogger(boolean isDefaultLogger)
    {
        LogUtil.isDefaultLogger = isDefaultLogger;
    }

    public synchronized static Logger logger(Class<?> clazz)
    {
        Logger logger = logger(clazz.getName());
        return logger;
    }

    public synchronized static Logger logger(String name)
    {
        OnGetLoggerListener onGetLogger = isDefaultLogger ? defaultOnGetLoggerListener : LISTENER_THREAD_LOCAL.get();
        if (onGetLogger == null)
        {
            onGetLogger = defaultOnGetLoggerListener;
        }
        if (onGetLogger == null)
        {
            throw new RuntimeException(OnGetLoggerListener.class.getName() + " is null!");
        }
        Logger logger = onGetLogger.getLogger(name);

        return logger;
    }

    public synchronized static void setOrRemoveOnGetLoggerListener(OnGetLoggerListener onGetLoggerListener)
    {
        if (onGetLoggerListener == null)
        {
            LISTENER_THREAD_LOCAL.remove();
        } else
        {
            LISTENER_THREAD_LOCAL.set(onGetLoggerListener);
        }
    }

    private static Map<String, Logger> loggerMap = new HashMap<>();

    public static synchronized Logger logger(OftenObject oftenObject, Class<?> clazz)
    {
        if (isDefaultLogger)
        {
            return defaultOnGetLoggerListener.getLogger(clazz.getName());
        }
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName());
        BridgeName bridgeName = oftenObject.getBridgeName();
        if (bridgeName != null)
        {
            builder.append(".").append(bridgeName.getName());
        }
        UrlDecoder.Result result = oftenObject.url();
        if (OftenTool.notEmpty(result.contextName()))
        {
            builder.append(".").append(result.contextName());
        }
        if (OftenTool.notEmpty(result.classTied()))
        {
            builder.append(".").append(result.classTied());
        }
        if (OftenTool.notEmpty(result.funTied()))
        {
            builder.append(".").append(result.funTied());
        }

        String key = builder.toString();
        Logger logger = loggerMap.get(key);
        if (logger == null)
        {
            logger = LoggerFactory.getLogger(key);
            loggerMap.put(key, logger);
        }
        return logger;
    }


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
        StackTraceElement stackTraceElement = stacks[n + 1];
        if (stackTraceElement.getClassName().equals(LogUtil.class.getName()))
        {
            stackTraceElement = stacks[n + 2];
        }
        return toString(stackTraceElement);
    }


    public static String[] listCodePos(int from, int maxCount)
    {
        if (from < 0)
        {
            throw new IllegalArgumentException("'from' have to be a positive value:" + from);
        }
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        List<String> list = new ArrayList<>();
        for (int i = from + 2, j = 0; i < stacks.length && j < maxCount; i++, j++)
        {
            list.add(toString(stacks[i]));
        }
        return list.toArray(new String[0]);
    }

    public static String getCodePosExceptNames(Set<String> exceptClassNames)
    {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        int n = 2;
        StackTraceElement target = null;
        Set<String> nameSet = exceptClassNames;
        while (n < stacks.length)
        {
            StackTraceElement element = stacks[n];
            if (nameSet.contains(element.getClassName()))
            {
                n++;
            } else
            {
                target = element;
                break;
            }
        }
        return toString(target);
    }

    public static String toString(StackTraceElement stackTraceElement)
    {
        if (stackTraceElement == null)
        {
            return "stacktrace null";
        }
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
     *
     * @param stack   0表示调用的地方，1表示上一个地方，依次类推。
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
     *
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yy/MM/dd HH:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * @param date 日期对象
     * @return
     */
    public static String getTime(Date date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yy/MM/dd HH:mm");
        return simpleDateFormat.format(date);
    }
}
