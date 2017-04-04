package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetMixin;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.sysset.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetSeek;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetUtil
{
    private final Logger LOGGER;

    private InnerContextBridge innerContextBridge;
    private Delivery thisDelivery;
    private PorterData porterData;

    public static AutoSetUtil newInstance(InnerContextBridge innerContextBridge, Delivery thisDelivery,
            PorterData porterData)
    {
        return new AutoSetUtil(innerContextBridge, thisDelivery, porterData);
    }

    private AutoSetUtil(InnerContextBridge innerContextBridge, Delivery thisDelivery, PorterData porterData)
    {
        this.innerContextBridge = innerContextBridge;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        LOGGER = LogUtil.logger(AutoSetUtil.class);
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
                doAutoSetSeek(packages[i], classLoader);
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void doAutoSetSeek(String packageStr, ClassLoader classLoader) throws Exception
    {
        LOGGER.debug("*****autoSetSeek******");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
            if (clazz.isAnnotationPresent(AutoSetSeek.class))
            {
                doAutoSet(clazz.newInstance(), null);
            }
        }
    }

    public synchronized void doAutoSetsForNotPorter(Object[] objects)
    {
        for (Object obj : objects)
        {
            doAutoSet(obj, null);
        }
    }

    public synchronized void doAutoSetForPorter(Object object, Map<String, Object> autoSetMixinMap)
    {
        doAutoSet(object, autoSetMixinMap);
    }

    private void doAutoSet(Object object, Map<String, Object> autoSetMixinMap)
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
                Object value = null;
                String autoSetMixinName = null;
                boolean needPut = false;
                if (autoSetMixinMap != null && f.isAnnotationPresent(AutoSetMixin.class))
                {
                    AutoSetMixin autoSetMixin = f.getAnnotation(AutoSetMixin.class);
                    autoSetMixinName = "".equals(autoSetMixin.value()) ? (autoSetMixin.classValue()
                            .equals(AutoSetMixin.class) ? f.getType().getName() : autoSetMixin.classValue()
                            .getName()) : autoSetMixin.value();

                    if (autoSetMixin.waitingForSet())
                    {
                        value = autoSetMixinMap.get(autoSetMixinName);
                    } else
                    {
                        needPut = true;
                    }
                }
                f.setAccessible(true);

                if (autoSet == null)
                {
                    if (value == null || autoSetMixinMap == null)
                    {
                        continue;
                    } else if (needPut)
                    {
                        autoSetMixinMap.put(autoSetMixinName, f.get(object));
                        continue;
                    }
                } else if (isDefaultAutoSetObject(f, object, autoSet))
                {
                    continue;
                }

                if (value == null)
                {
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
                    } else if (needPut)
                    {
                        autoSetMixinMap.put(autoSetMixinName, value);
                    }
                }
                f.set(object, value);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("AutoSet:({})[{}] with [{}]", autoSetMixinName == null ? "" : AutoSetMixin.class
                            .getSimpleName() + " " + (needPut ? "get" : "set") + " " + autoSetMixinName, f, value);
                }
                doAutoSet(value, autoSetMixinMap);//设置被设置的变量。
            } catch (Exception e)
            {
                LOGGER.warn("AutoSet failed for [{}]({}),ex={}", f, autoSet.range(), e.getMessage());
            }

        }
        if (thr != null)
        {
            throw thr;
        } else
        {
            Method[] methods = WPTool.getAllPublicMethods(object.getClass());
            try
            {
                for (Method method : methods)
                {
                    if (method.isAnnotationPresent(AutoSet.SetOk.class))
                    {
                        method.invoke(object);
                    }
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 是否是默认工具类。
     */
    private boolean isDefaultAutoSetObject(Field f, Object object, AutoSet autoSet) throws IllegalAccessException
    {
        Object sysset = null;
        String typeName = f.getType().getName();
        if (typeName.equals(TypeTo.class.getName()))
        {
            sysset = new TypeTo(innerContextBridge);
        } else if (typeName.equals(PorterData.class.getName()))
        {
            sysset = porterData;
        } else if (typeName.equals(Delivery.class.getName()))
        {
            String pName = autoSet.value();
            Delivery delivery;
            if (WPTool.isEmpty(pName))
            {
                delivery = thisDelivery;
            } else
            {
                CommonMain commonMain = PorterMain.getMain(pName);
                if (commonMain == null)
                {
                    throw new Error(
                            String.format("%s object is null for %s[%s]!", AutoSet.class.getSimpleName(), f, pName));
                }
                delivery = commonMain.getPLinker();
            }

            sysset = delivery;
        }
        if (sysset != null)
        {
            f.set(object, sysset);
            LOGGER.debug("AutoSet [{}] with default object [{}]", f, sysset);
        }
        return sysset != null;
    }
}
