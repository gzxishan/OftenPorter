package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.MixinTo;
import cn.xishan.oftenporter.porter.core.annotation.PortInit;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.init.PortIniter;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{

    public static interface Fun
    {
        Object getFinalPorterObject();

        OftenEntities getOftenEntities();

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
        public OftenEntities getOftenEntities()
        {
            return fun.getOftenEntities();
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

    // Class[] superGenericClasses;

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

    OftenEntities oftenEntities;
    private TypeParserStore typeParserStore;

    private AutoSetHandle autoSetHandle;
    private OftenContextInfo contextInfo;

    public Porter(Class clazz, AutoSetHandle autoSetHandle, WholeClassCheckPassableGetter wholeClassCheckPassableGetter)
    {
        this.clazz = clazz;
        this.finalPorter = this;
        this.typeParserStore = autoSetHandle.getInnerContextBridge().innerBridge.globalParserStore;
        LOGGER = LogUtil.logger(Porter.class);
        this.autoSetHandle = autoSetHandle;
        this.contextInfo = autoSetHandle.getOftenContextInfo();
        this.wholeClassCheckPassableGetter = wholeClassCheckPassableGetter;
    }


    private static final String TYPE_NAME_PREFIX = "class ";

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public OftenContextInfo getContextInfo()
    {
        return contextInfo;
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

    public OftenEntities getOftenEntities()
    {
        return oftenEntities;
    }

    public _PortIn getPortIn()
    {
        return portIn;
    }


    void addAutoSet() throws Exception
    {
        this.object = autoSetHandle.addAutoSetForPorter(this);
        finalObject = object;
    }


    public Class<?> getClazz()
    {
        return clazz;
    }

    public Object getObj()
    {
        return object;
    }

    public void setObj(Object object)
    {
        this.object = object;
    }

    /**
     * 对于rest，会优先获取非{@linkplain TiedType#METHOD}接口。
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
        PorterOfFun porterOfFun = childrenWithMethod.get(funTied + TIED_KEY_SEPARATOR + method.name());//METHOD
        if (porterOfFun == null)
        {
            porterOfFun = childrenWithMethod.get(method.name());
        }

        return porterOfFun;
    }

    public void dealInNames(TypeParserStore typeParserStore)
    {
        //处理dealtFor

        if (oftenEntities != null)
        {
            for (One one : oftenEntities.ones)
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
                if (porter.oftenEntities != null)
                {
                    for (One one : porter.oftenEntities.ones)
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
            if (porterOfFun.oftenEntities != null)
            {
                for (One one : porterOfFun.oftenEntities.ones)
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
            dealName(name);
        }
    }

    public InNames.Name getName(AnnotationDealt annotationDealt, String varName, Class<?> type, _Parse parse,
            _Nece nece) throws ClassNotFoundException
    {
        InNames.Name theName = OftenEntitiesDeal.getName(annotationDealt, nece, varName, type, typeParserStore, false);
        SthUtil.bindTypeParse(InNames.temp(theName), parse, typeParserStore, null,
                BackableSeek.SeekType.NotAdd_NotBind);
        dealName(theName);
        return theName;
    }

    private void dealName(InNames.Name name)
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

    public void start(OftenObject oftenObject, IConfigData iConfigData)
    {
        start(oftenObject, iConfigData, false);
        autoSetHandle = null;
    }

    public void initArgsHandle(IArgumentsFactory iArgumentsFactory) throws Exception
    {
        for (PorterOfFun fun : childrenWithMethod.values())
        {
            iArgumentsFactory.initArgsHandle(fun, typeParserStore);
        }
    }

    public void initOftenEntitiesHandle(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge) throws Exception
    {
        initOftenEntitiesHandle(oftenEntities);
        for (PorterOfFun fun : childrenWithMethod.values())
        {
            fun.initEntities(extraEntityMap, sthDeal, innerContextBridge, autoSetHandle);
        }
    }

    void initOftenEntitiesHandle(OftenEntities entities)
    {
        if (entities == null)
        {
            return;
        }
        initOftenEntitiesHandle(entities.ones);
    }

    void initOftenEntitiesHandle(One... ones)
    {

        for (One one : ones)
        {
            _BindEntities.CLASS clazz = one.getEntityClazz();
            if (clazz != null)
            {
                try
                {
                    clazz.init();
                } catch (Exception e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private void start(OftenObject oftenObject, IConfigData iConfigData, boolean isMixin)
    {
        if (started)
        {
            return;
        } else
        {
            started = true;
        }

        MixinListener mixinListener = null;
        if(getObj() instanceof MixinListener){
            mixinListener= (MixinListener) getObj();
        }
        List<Porter> mixinList = null;
        if(mixinListener!=null){
            mixinList=new ArrayList<>();
            if(mixins!=null){
                OftenTool.addAll(mixinList,mixins);
            }
        }

        if(mixinListener!=null){
            mixinListener.beforeStartOfMixin(mixinList);
        }
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                porter.start(oftenObject, iConfigData, true);
            }
        }
        if(mixinListener!=null){
            mixinListener.afterStartOfMixin(mixinList);
        }
        _PortStart[] starts = getStarts();
        oftenObject.pushClassTied(getPortIn().getTiedNames()[0]);
        for (int i = 0; i < starts.length; i++)
        {
            try
            {
                PorterOfFun porterOfFun = starts[i].getPorterOfFun();
                DefaultArgumentsFactory.invokeWithArgs(iConfigData, porterOfFun.getObject(),
                        porterOfFun.getMethod(), oftenObject, iConfigData);
            } catch (Exception e)
            {
                if (LOGGER.isWarnEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.warn(throwable.getMessage(), throwable);
                }
            }
        }
        if (!isMixin)
        {
            for (PorterOfFun porterOfFun : childrenWithMethod.values())
            {
                porterOfFun.startHandles(oftenObject);
            }
        }
        oftenObject.popClassTied();

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
                if (LOGGER.isWarnEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.warn(throwable.getMessage(), throwable);
                }
            }
        }
    }


    class PortIniterImpl extends PortIniter
    {

        PortMethod method;
        String path, fkey;
        BridgeResponse result;

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
            BridgeRequest request = new BridgeRequest(path);
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
            case METHOD:
            case FORCE_METHOD:
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
            MixinTo[] mixinTos = PortUtil.getMixinTos(clazz);
            for (MixinTo mixinTo : mixinTos)
            {
                if (!AutoSet.class.equals(mixinTo.toContextSetWithClassKey()))
                {
                    Class key = mixinTo.toContextSetWithClassKey();
                    map.put(key, porter);
                }
            }
            porter.getMixinToThatCouldSet(map);
        }
    }

    @Override
    public String toString()
    {
        return super.toString()+"->"+getObj();
    }
}
