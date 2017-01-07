package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetSeek;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSetUtil.class);

    private InnerContextBridge innerContextBridge;

    public AutoSetUtil(InnerContextBridge innerContextBridge)
    {
        this.innerContextBridge = innerContextBridge;
    }

    public InnerContextBridge getInnerContextBridge()
    {
        return innerContextBridge;
    }

    public synchronized void doAutoSetSeek(String[] packages, ClassLoader classLoader)
    {
        if (packages == null)
        {
            return;
        }
        try
        {
            for (int i = 0; i < packages.length; i++)
            {
                seekPackage(packages[i], classLoader);
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void seekPackage(String packageStr, ClassLoader classLoader) throws Exception
    {
        LOGGER.debug("*****autoSetSeek******");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
            if(clazz.isAnnotationPresent(AutoSetSeek.class)){
                doAutoSet(clazz.newInstance());
            }
        }
    }

    public synchronized void doAutoSet(Object object)
    {

        Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
        Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
        Field[] fields = WPTool.getAllFields(object.getClass());
        RuntimeException thr = null;
        for (int i = 0; i < fields.length; i++)
        {

            Field f = fields[i];
            AutoSet autoSet = f.getAnnotation(AutoSet.class);
            try
            {
                if (autoSet == null)
                {
                    continue;
                }
                f.setAccessible(true);
                if (isDefaultAutoSetObject(f, object))
                {
                    continue;
                }

                String keyName;
                Class<?> mayNew = null;
                Class<?> classClass = autoSet.classValue();
                if (classClass.equals(AutoSet.class))
                {
                    keyName = autoSet.value();
                } else
                {
                    keyName = classClass.getName();
                    mayNew = classClass;
                }
                if ("".equals(keyName))
                {
                    keyName = f.getType().getName();
                }
                if (mayNew == null)
                {
                    mayNew = f.getType();
                }
                Object value = null;
                switch (autoSet.range())
                {
                    case Global:
                    {
                        value = globalAutoSet.get(keyName);
                        if (value == null)
                        {
                            value = WPTool.newObject(mayNew);
                            globalAutoSet.put(keyName, value);
                        }
                    }
                    break;
                    case Context:
                    {
                        value = contextAutoSet.get(keyName);
                        if (value == null)
                        {
                            value = WPTool.newObject(mayNew);
                            contextAutoSet.put(keyName, value);
                        }
                    }
                    break;
                    case New:
                    {
                        value = WPTool.newObject(mayNew);
                    }
                    break;
                }
                if (value == null)
                {
                    thr = new RuntimeException(String.format("AutoSet:could not set [%s] with null!", f));
                    break;
                }
                f.set(object, value);
                LOGGER.debug("AutoSet [{}] with [{}]", f, value);
                doAutoSet(value);//设置被设置的变量。
            } catch (Exception e)
            {
                LOGGER.error("AutoSet failed for [{}]({}),ex={}", f, autoSet.range(), e.getMessage());
            }

        }
        if (thr != null)
        {
            throw thr;
        }
    }

    /**
     * 是否是默认工具类。
     */
    private boolean isDefaultAutoSetObject(Field f, Object object) throws IllegalAccessException
    {
        if (!f.getType().getName().equals(TypeTo.class.getName()))
        {
            return false;
        }

        TypeTo typeTo = new TypeTo(innerContextBridge);
        f.set(object, typeTo);
        LOGGER.debug("AutoSet [{}] with default object [{}]", f, typeTo);
        return true;
    }
}
