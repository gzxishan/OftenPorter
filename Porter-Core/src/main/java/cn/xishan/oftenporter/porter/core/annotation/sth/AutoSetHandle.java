package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetMixin;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetHandle
{
    private final Logger LOGGER;

    private InnerContextBridge innerContextBridge;
    private Delivery thisDelivery;
    private PorterData porterData;
    private List<IHandle> iHandles = new ArrayList<>();


    private interface IHandle
    {
        void handle() throws FatalInitException;
    }

    private class Handle_doAutoSetThatOfMixin implements IHandle
    {
        Object[] args;

        public Handle_doAutoSetThatOfMixin(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetThatOfMixin(Object obj1, Object obj2) throws FatalInitException
        {
            try
            {
                _doAutoSetThatOfMixin(obj1, obj2);
                _doAutoSetThatOfMixin(obj2, obj1);
            } catch (Exception e)
            {
                throw new FatalInitException(e);
            }
        }

        @Override
        public void handle() throws FatalInitException
        {
            this.doAutoSetThatOfMixin(args[0], args[1]);
        }
    }

    private class Handle_doAutoSetsForNotPorter implements IHandle
    {

        private Object[] objects;

        public Handle_doAutoSetsForNotPorter(Object[] objects)
        {
            this.objects = objects;
        }

        private void doAutoSetsForNotPorter(Object[] objects)
        {
            for (Object obj : objects)
            {
                doAutoSet(obj, null);
            }
        }

        @Override
        public void handle()
        {
            this.doAutoSetsForNotPorter(objects);
        }
    }

    private class Handle_doAutoSetSeek implements IHandle
    {

        Object[] args;

        public Handle_doAutoSetSeek(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetSeek(String[] packages, ClassLoader classLoader)
        {
            if (packages == null)
            {
                return;
            }
            try
            {
                for (int i = 0; i < packages.length; i++)
                {
                    AutoSetHandle.this.doAutoSetSeek(packages[i], classLoader);
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle()
        {
            this.doAutoSetSeek((String[]) args[0], (ClassLoader) args[1]);
        }
    }

    private class Handle_doAutoSetForPorter implements IHandle
    {
        Object[] args;

        public Handle_doAutoSetForPorter(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetForPorter(Object object, Map<String, Object> autoSetMixinMap)
        {
            doAutoSet(object, autoSetMixinMap);
        }

        @Override
        public void handle()
        {
            this.doAutoSetForPorter(args[0], (Map<String, Object>) args[1]);
        }
    }

    public static AutoSetHandle newInstance(InnerContextBridge innerContextBridge, Delivery thisDelivery,
            PorterData porterData)
    {
        return new AutoSetHandle(innerContextBridge, thisDelivery, porterData);
    }

    private AutoSetHandle(InnerContextBridge innerContextBridge, Delivery thisDelivery, PorterData porterData)
    {
        this.innerContextBridge = innerContextBridge;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        LOGGER = LogUtil.logger(AutoSetHandle.class);
    }

    public InnerContextBridge getInnerContextBridge()
    {
        return innerContextBridge;
    }

    public synchronized void addAutoSetSeek(String[] packages, ClassLoader classLoader)
    {
        iHandles.add(new Handle_doAutoSetSeek(packages, classLoader));
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

    public synchronized void addAutoSetsForNotPorter(Object[] objects)
    {
        iHandles.add(new Handle_doAutoSetsForNotPorter(objects));
    }

    public synchronized void addAutoSetForPorter(Object object, Map<String, Object> autoSetMixinMap)
    {
        iHandles.add(new Handle_doAutoSetForPorter(object, autoSetMixinMap));
        //doAutoSet(object, autoSetMixinMap);
    }

    public synchronized void addAutoSetThatOfMixin(Object obj1, Object obj2)
    {
        iHandles.add(new Handle_doAutoSetThatOfMixin(obj1, obj2));
    }

    public synchronized void doAutoSet() throws FatalInitException
    {
        try
        {
            for (int i = 0; i < iHandles.size(); i++)
            {

                iHandles.get(i).handle();

            }
        } catch (FatalInitException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new FatalInitException(e);
        } finally
        {
            iHandles.clear();
        }
    }

    private void _doAutoSetThatOfMixin(Object obj1, Object objectForSet) throws Exception
    {
        Field[] fields = obj1.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(AutoSet.AutoSetThatOfMixin.class) && WPTool
                    .isAssignable(objectForSet, field.getType()))
            {
                field.setAccessible(true);
                field.set(obj1, objectForSet);
                LOGGER.debug("AutoSet.AutoSetThatOfMixin:[{}] with [{}]", field, objectForSet);
            }
        }
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
                AutoSetMixin autoSetMixin = null;
                if (autoSetMixinMap != null && f.isAnnotationPresent(AutoSetMixin.class))
                {
                    autoSetMixin = f.getAnnotation(AutoSetMixin.class);
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
        } else if (typeName.equals(Logger.class.getName()))
        {
            sysset = LogUtil.logger(object.getClass());
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
