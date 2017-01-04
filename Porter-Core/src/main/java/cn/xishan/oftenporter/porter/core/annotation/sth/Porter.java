package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Porter.class);

    Object object;
    Class<?> clazz;
    _PortIn portIn;

    _PortStart[] starts;
    _PortDestroy[] destroys;
    Map<String, PorterOfFun> children;

    InObj inObj;
    private InnerContextBridge innerContextBridge;

    public Porter(InnerContextBridge innerContextBridge)
    {
        this.innerContextBridge = innerContextBridge;
    }

    public Map<String, PorterOfFun> getChildren()
    {
        return children;
    }

    public _PortStart[] getStarts()
    {
        return starts;
    }

    public _PortDestroy[] getDestroys()
    {
        return destroys;
    }

    public InObj getInObj()
    {
        return inObj;
    }

    public _PortIn getPortIn()
    {
        return portIn;
    }

    void doAutoSet()
    {
        if (object == null)
        {
            return;
        }
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
                    thr = new RuntimeException(String.format("AutoSet:could not set [%s]", f));
                    break;
                }
                f.set(object, value);
                LOGGER.debug("AutoSet [{}] with [{}]", f, value);
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

    public Class<?> getClazz()
    {
        return clazz;
    }

    public Object getObject()
    {
        if (object == null)
        {
            try
            {
                object = WPTool.newObject(clazz);
                doAutoSet();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return object;
    }

    /**
     * 对于rest，会优先获取非{@linkplain TiedType#REST}接口。
     *
     * @param result 地址解析结果
     * @param method 请求方法
     * @return 函数接口。
     */
    public PorterOfFun getChild(UrlDecoder.Result result, PortMethod method)
    {
        PorterOfFun porterOfFun = null;

        _PortIn portIn = getPortIn();
        switch (portIn.getTiedType())
        {

            case REST:
                if (portIn.isMultiTiedType())
                {
                    porterOfFun = children.get(result.funTied());
                    if (porterOfFun == null)
                    {
                        porterOfFun = children.get(method.name());
                    }
                } else
                {
                    porterOfFun = children.get(method.name());
                }
                break;
            case Default:
                porterOfFun = children.get(result.funTied());
                break;
        }
        if (porterOfFun != null && porterOfFun.getPortIn().getMethod() != method)
        {
            porterOfFun = null;
        }
        return porterOfFun;
    }
}
