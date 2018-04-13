package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.PortInit;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PortIniter;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{

    public static interface Fun
    {
        Object getFinalPorterObject();

        InObj getInObj();

        Method getMethod();

        /**
         * 得到函数所在的对象实例。
         *
         * @return
         */
        Object getObject();

        OutType getOutType();

        InNames getInNames();

        PortMethod getPortMethod();

        TiedType getTiedType();

        PortFunType getPortFunType();

        String getTiedName();
    }

    private static class FunImpl implements Fun
    {
        String tiedName;
        PortMethod portMethod;
        PorterOfFun fun;

        public FunImpl(String tiedNameWithMethod, PorterOfFun fun)
        {
            int index = tiedNameWithMethod.indexOf(TIED_KEY_SEPARATOR);
            if (index >= 0)
            {
                this.tiedName = tiedNameWithMethod.substring(0, index);
                portMethod = PortMethod.valueOf(tiedNameWithMethod.substring(index + 1));
            } else
            {
                this.tiedName = "";
                portMethod = PortMethod.valueOf(tiedNameWithMethod);
            }

            this.fun = fun;
        }

        @Override
        public PortMethod getPortMethod()
        {
            return portMethod;
        }

        @Override
        public Object getFinalPorterObject()
        {
            return fun.getFinalPorterObject();
        }

        @Override
        public InObj getInObj()
        {
            return fun.getInObj();
        }

        @Override
        public Method getMethod()
        {
            return fun.getMethod();
        }

        @Override
        public Object getObject()
        {
            return fun.getObject();
        }

        @Override
        public OutType getOutType()
        {
            return fun.getPortOut().getOutType();
        }

        @Override
        public InNames getInNames()
        {
            return fun.getMethodPortIn().getInNames();
        }


        @Override
        public TiedType getTiedType()
        {
            return fun.getMethodPortIn().getTiedType();
        }

        @Override
        public PortFunType getPortFunType()
        {
            return fun.getMethodPortIn().getPortFunType();
        }

        @Override
        public String getTiedName()
        {
            return tiedName;
        }
    }

    Class[] superGenericClasses;

    private final Logger LOGGER;
    private boolean started = false, destroyed = false;

    Object object;
    Class<?> clazz;
    _PortIn portIn;
    Object finalObject;
    _PortOut portOut;

    Porter finalPorter;

    WholeClassCheckPassableGetter wholeClassCheckPassableGetter;

    public static final String TIED_KEY_SEPARATOR = ":";

    _PortStart[] starts;
    _PortDestroy[] destroys;
    /**
     * {"funTied:method"或者"method":PorterOfFun}
     */
    Map<String, PorterOfFun> childrenWithMethod;
    Porter[] mixins;

    InObj inObj;
    private AutoSetHandle autoSetHandle;

    public Porter(Class clazz, AutoSetHandle autoSetHandle, WholeClassCheckPassableGetter wholeClassCheckPassableGetter)
    {
        this.clazz = clazz;
        this.finalPorter = this;
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
     * 获取最终的接口Porter.
     *
     * @return
     */
    public Porter getFinalPorter()
    {
        return finalPorter;
    }

    /**
     * 获取绑定的函数：{"funTied:method"或者"method":Fun}
     *
     * @return
     */
    public Map<String, Fun> getFuns()
    {
        Map<String, Fun> map = new HashMap<>(childrenWithMethod.size());
        for (Map.Entry<String, PorterOfFun> entry : childrenWithMethod.entrySet())
        {
            map.put(entry.getKey(), new FunImpl(entry.getKey(), entry.getValue()));
        }
        return map;
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


    void addAutoSet()
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
        finalObject = object;
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
        PorterOfFun porterOfFun = childrenWithMethod.get(funTied + TIED_KEY_SEPARATOR + method.name());
        if (porterOfFun == null)
        {
            porterOfFun = childrenWithMethod.get(method.name());
        }

        return porterOfFun;
    }

    public void dealInNames(TypeParserStore typeParserStore)
    {
        //处理dealtFor

        if (inObj != null)
        {
            for (One one : inObj.ones)
            {
                dealInNames(one.inNames, typeParserStore);
            }
        }


        InNames inNames = getPortIn().getInNames();
        dealInNames(inNames, typeParserStore);
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                if (porter.inObj != null)
                {
                    for (One one : porter.inObj.ones)
                    {
                        dealInNames(one.inNames, typeParserStore);
                    }
                }
                dealInNames(porter.getPortIn().getInNames(), typeParserStore);
            }
        }
        for (Map.Entry<String, PorterOfFun> entry : childrenWithMethod.entrySet())
        {
            PorterOfFun porterOfFun = entry.getValue();
            if (porterOfFun.inObj != null)
            {
                for (One one : porterOfFun.inObj.ones)
                {
                    dealInNames(one.inNames, typeParserStore);
                }
            }
            dealInNames(porterOfFun.getMethodPortIn().getInNames(), typeParserStore);
        }
    }

    private void dealInNames(InNames inNames, TypeParserStore typeParserStore)
    {
        dealInNames(inNames.nece, typeParserStore);
        dealInNames(inNames.unece, typeParserStore);
    }

    private void dealInNames(InNames.Name[] names, TypeParserStore typeParserStore)
    {
        for (InNames.Name name : names)
        {
            if (name.typeParserId == null)
            {
                name.typeParserId = typeParserStore.getDefaultTypeParserId();
            }
            ITypeParser typeParser = typeParserStore.byId(name.typeParserId);
            if (typeParser != null)
            {
                name.doDealtFor(typeParser);
            }
        }
    }

    public void start(WObject wObject)
    {
        start(wObject, false);
        autoSetHandle=null;
    }

    public void initIInObjHandle()
    {
        initIInObjHandle(inObj);
        for (PorterOfFun fun : childrenWithMethod.values())
        {
            initIInObjHandle(fun.getInObj());
        }
    }

    private void initIInObjHandle(InObj inObj)
    {
        if (inObj == null)
        {
            return;
        }
        for (One one : inObj.ones)
        {
            _PortInObj.CLASS clazz = one.getInObjClazz();
            if (clazz != null)
            {
                clazz.init();
            }
        }
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


    class PortIniterImpl extends PortIniter
    {

        PortMethod method;
        String path, fkey;
        PResponse result;

        public PortIniterImpl(String path, String fkey, int order, PortMethod method)
        {
            super(path + ":" + method.name(), order);
            this.path = path;
            this.fkey = fkey + ":" + method.name();
            this.method = method;
        }

        @Override
        public synchronized void init(Delivery delivery) throws InitException
        {
            PRequest request = new PRequest(path);
            result = null;
            request.setMethod(method);
            delivery.innerBridge().request(request, lResponse -> result = lResponse);
            childrenWithMethod.remove(fkey);//移除
            LOGGER.debug("removed:{}:{}", path, method);
            if (result != null)
            {
                if (!result.isOk())
                {
                    throw new InitException("init fail for '" + path + "':" + result.getResponse());
                }
                Object rs = result.getResponse();
                if (rs != null && (rs instanceof JResponse) && ((JResponse) rs).isNotSuccess())
                {
                    throw new InitException("init fail for '" + path + "':" + rs);
                }
            }
        }
    }

    public void seekPortInit(String contextName, List<PortIniter> portIniterList)
    {

        Iterator<PorterOfFun> iterator = childrenWithMethod.values().iterator();
        while (iterator.hasNext())
        {
            PorterOfFun porterOfFun = iterator.next();
            PortInit portInit = AnnoUtil.getAnnotation(porterOfFun.getMethod(), PortInit.class);
            PortIniter portIniter = newIniter(porterOfFun, portInit, porterOfFun.getMethodPortIn().getTiedType(),
                    contextName);
            if (portIniter != null)
            {
                portIniterList.remove(portIniter);
                portIniterList.add(portIniter);
            }
        }
    }

    private PortIniter newIniter(PorterOfFun porterOfFun, @MayNull PortInit portInit, TiedType tiedType,
            String contextName)
    {
        if (portInit == null)
        {
            return null;
        }

        if (porterOfFun.getMethodPortIn().getMethods().length > 1)
        {
            throw new InitException("methods length > 1 for @" + PortInit.class.getSimpleName());
        }
        if (porterOfFun.getMethodPortIn().getTiedNames().length > 1)
        {
            throw new InitException("tieds length > 1 for @" + PortInit.class.getSimpleName());
        }
        String path = null, fkey = null;
        PortMethod method = porterOfFun.getMethodPortIn().getMethods()[0];
        String classTied = this.getPortIn().getTiedNames()[0];
        String funTied = porterOfFun.getMethodPortIn().getTiedNames()[0];

        switch (tiedType)
        {
            case REST:
            case FORCE_REST:
                path = "/" + contextName + "/" + classTied + "/";
                fkey = "";
                break;
            case DEFAULT:
                path = "/" + contextName + "/" + classTied + "/" + funTied;
                fkey = funTied;
                break;
        }
        PortIniter portIniter = new PortIniterImpl(path, fkey, portInit.order(), method);
        return portIniter;
    }

    public Map<Class, Porter> getMixinToThatCouldSet()
    {
        Map<Class, Porter> map = new HashMap<>();
        getMixinToThatCouldSet(map);
        return map;
    }

    void getMixinToThatCouldSet(Map<Class, Porter> map)
    {
        if (mixins == null)
        {
            return;
        }
        for (Porter porter : mixins)
        {
            Class key = PortUtil.getMixinToContextSetKey(porter.getClazz());
            if (key != null)
            {
                map.put(key, porter);
            }
            porter.getMixinToThatCouldSet(map);
        }
    }
}
