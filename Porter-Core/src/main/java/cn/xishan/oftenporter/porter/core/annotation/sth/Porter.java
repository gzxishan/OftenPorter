package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{

    Class[] superGenericClasses;

    private final Logger LOGGER;
    private boolean started = false, destroyed = false;

    Object object;
    Class<?> clazz;
    _PortIn portIn;
    Object finalObject;
    _PortOut portOut;

    WholeClassCheckPassableGetter wholeClassCheckPassableGetter;

    _PortStart[] starts;
    _PortDestroy[] destroys;
    /**
     * {"funTied/method"或者"method":PorterOfFun}
     */
    Map<String, PorterOfFun> childrenWithMethod;
    Porter[] mixins;

    InObj inObj;
    private AutoSetHandle autoSetHandle;

    public Porter(Class clazz, AutoSetHandle autoSetHandle, WholeClassCheckPassableGetter wholeClassCheckPassableGetter)
    {
        this.clazz = clazz;
        LOGGER = LogUtil.logger(Porter.class);
        this.autoSetHandle = autoSetHandle;
        this.wholeClassCheckPassableGetter = wholeClassCheckPassableGetter;
        try
        {
            initSuperGenericClasses();
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }


    //泛型处理
    private void initSuperGenericClasses() throws Exception
    {

        Type superclassType = clazz.getGenericSuperclass();
        if (!ParameterizedType.class.isAssignableFrom(superclassType.getClass()))
        {
            return;
        }

        List<Class> list = new ArrayList<>();

        Type[] types = ((ParameterizedType) superclassType).getActualTypeArguments();
        ClassLoader classLoader = autoSetHandle.getInnerContextBridge().classLoader;
        for (Type type : types)
        {
            String className = getClassName(type);
            if (className == null)
            {
                continue;
            }
            list.add(PackageUtil.newClass(className, classLoader));
        }
        superGenericClasses = list.toArray(new Class[0]);
    }

    private static final String TYPE_NAME_PREFIX = "class ";

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public String getContextName()
    {
        return autoSetHandle.getContextName();
    }

    public PName getPName()
    {
        return autoSetHandle.getPName();
    }

    public static String getClassName(Type type)
    {
        if (type == null)
        {
            return null;
        }
        String className = type.toString();
        if (className.startsWith(TYPE_NAME_PREFIX))
        {
            className = className.substring(TYPE_NAME_PREFIX.length());
        }
        return className;
    }

    /**
     * 获取正确的变量类型。
     *
     * @return
     */
    Class getFieldRealClass(Field field)
    {
        Class<?> ftype = field.getType();
        if (field.getGenericType() == null || superGenericClasses == null)
        {
            return ftype;
        }
        for (int i = 0; i < superGenericClasses.length; i++)
        {
            if (WPTool.isAssignable(superGenericClasses[i], ftype))
            {
                return superGenericClasses[i];
            }
        }
        return ftype;
    }

    public WholeClassCheckPassableGetter getWholeClassCheckPassableGetter()
    {
        return wholeClassCheckPassableGetter;
    }

    /**
     * 获取最终的接口对象，对于一般接口，该对象等于{@linkplain #getObj()}(即当前接口类本身)；对于混入接口的情况，如把接口A（含有接口函数a）混入到B中，则调用B/a接口函数时，返回的最终对象为B的实例。
     *
     * @return
     */
    public Object getFinalPorterObject()
    {
        return finalObject;
    }

    /**
     * 获取绑定的函数：{"funTied/method"或者"method":PorterOfFun}
     *
     * @return
     */
    public Map<String, PorterOfFun> getFuns()
    {
        return childrenWithMethod;
    }


    private _PortStart[] getStarts()
    {
        return starts;
    }

    private _PortDestroy[] getDestroys()
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
            try
            {
                object = WPTool.newObject(clazz);
            } catch (Exception e)
            {
                throw new InitException(e);
            }
        }
        autoSetHandle.addAutoSetForPorter(this);
    }


    public Class<?> getClazz()
    {
        return clazz;
    }

    public Object getObj()
    {
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
        PorterOfFun porterOfFun = getChild(result.funTied(), method);
        return porterOfFun;
    }

    public PorterOfFun getChild(String funTied, PortMethod method)
    {
        PorterOfFun porterOfFun;
//        switch (classTiedType)
//        {
//
//            case FORCE_REST:
//            case REST:
//                porterOfFun = childrenWithMethod.get(funTied + "/" + method.name());
//                if (porterOfFun == null)
//                {
//                    porterOfFun = childrenWithMethod.get(method.name());
//                }
//                break;
//            case DEFAULT:
//                porterOfFun = childrenWithMethod.get(funTied + "/" + method.name());
//                if(porterOfFun==null){
//                    porterOfFun = childrenWithMethod.get(method.name());
//                }
//                break;
//        }

//        if (porterOfFun != null && porterOfFun.getMethodPortIn().getMethod() != method)
//        {
//            porterOfFun = null;
//        }

        porterOfFun = childrenWithMethod.get(funTied + "/" + method.name());
        if (porterOfFun == null)
        {
            porterOfFun = childrenWithMethod.get(method.name());
        }

        return porterOfFun;
    }

    public void start(WObject wObject)
    {
        start(wObject, false);
    }

    private void start(WObject wObject, boolean isMixin)
    {
        if (started)
        {
            return;
        } else
        {
            started = true;
        }
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                porter.start(wObject, true);
            }
        }
        _PortStart[] starts = getStarts();
        wObject.pushClassTied(getPortIn().getTiedNames()[0]);
        for (int i = 0; i < starts.length; i++)
        {
            try
            {
                PorterOfFun porterOfFun = starts[i].getPorterOfFun();
                Method method = porterOfFun.getMethod();
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1)
                {
                    method.invoke(porterOfFun.getObject(), wObject);
                } else
                {
                    method.invoke(porterOfFun.getObject());
                }
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        if (!isMixin)
        {
            for (PorterOfFun porterOfFun : childrenWithMethod.values())
            {
                porterOfFun.startHandles(wObject);
            }
        }
        wObject.popClassTied();

    }

    public void destroy()
    {
        destroy(false);
    }

    private void destroy(boolean isMixin)
    {
        if (destroyed)
        {
            return;
        } else
        {
            destroyed = true;
        }
        if (!isMixin)
        {
            for (PorterOfFun porterOfFun : childrenWithMethod.values())
            {
                porterOfFun.destroyHandles();
            }
        }
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                porter.destroy(true);
            }
        }
        _PortDestroy[] ds = getDestroys();
        for (int i = 0; i < ds.length; i++)
        {
            try
            {
                PorterOfFun porterOfFun = ds[i].getPorterOfFun();
                porterOfFun.getMethod().invoke(porterOfFun.getObject());
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
//        childrenWithMethod.clear();
//        childrenWithMethod=null;
//        mixins = null;
//        destroys = null;
//        starts = null;
//        object = null;
//        clazz = null;
//        portIn = null;
//        inObj=null;
//        autoSetUtil=null;
    }
}
