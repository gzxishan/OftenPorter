package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.PortExecutor;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public class SthDeal
{
    private final Logger LOGGER;
    private InObjDeal inObjDeal;

    public SthDeal()
    {
        LOGGER = LogUtil.logger(SthDeal.class);
        inObjDeal = new InObjDeal();
    }


    private boolean mayAddStartOrDestroy(Method method, ObjectGetter objectGetter, List<_PortStart> portStarts,
            List<_PortDestroy> portDestroys,
            AnnotationDealt annotationDealt)
    {
        _PortStart portStart = annotationDealt.portStart(method, objectGetter);
        if (portStart != null)
        {
            portStarts.add(portStart);
        }
        _PortDestroy portDestroy = annotationDealt.portDestroy(method, objectGetter);
        if (portDestroy != null)
        {
            portDestroys.add(portDestroy);
        }
        return portStart != null || portDestroy != null;
    }


    public void dealPortAB(Context context, PortExecutor portExecutor) throws FatalInitException
    {
        SthUtil sthUtil = new SthUtil();
        sthUtil.expandPortAB(context, portExecutor);
    }


    private void addPortAfterBefore(Porter porter, String currentContextName, String currentClassTied,
            AutoSetHandle autoSetHandle)
    {
        List<_PortFilterOne> portBeforesAll = autoSetHandle.getInnerContextBridge().annotationDealt
                .portBefores(porter.getClazz(), currentContextName, currentClassTied);
        List<_PortFilterOne> portAftersAll = autoSetHandle.getInnerContextBridge().annotationDealt
                .portAfters(porter.getClazz(), currentContextName, currentClassTied);
        for (PorterOfFun porterOfFun : porter.getFuns().values())
        {
            Method method = porterOfFun.method;
            List<_PortFilterOne> portBefores = autoSetHandle.getInnerContextBridge().annotationDealt
                    .portBefores(method, currentContextName, currentClassTied);
            List<_PortFilterOne> portAfters = autoSetHandle.getInnerContextBridge().annotationDealt
                    .portAfters(method, currentContextName, currentClassTied);

            for (int i = portBeforesAll.size() - 1; i >= 0; i--)
            {
                portBefores.add(0, portBeforesAll.get(i));
            }
            portAfters.addAll(portAftersAll);

            porterOfFun.portBefores = portBefores.toArray(new _PortFilterOne[0]);
            porterOfFun.portAfters = portAfters.toArray(new _PortFilterOne[0]);
        }
    }


    public Porter porter(Class<?> clazz, Object object, String currentContextName,
            AutoSetHandle autoSetHandle) throws FatalInitException
    {
        return porter(clazz, object, currentContextName, null, autoSetHandle, false, null, null);
    }


    private Porter porter(Class<?> clazz, Object object, String currentContextName, String currentClassTied,
            AutoSetHandle autoSetHandle,
            boolean isMixin, WholeClassCheckPassableGetterImpl wholeClassCheckPassableGetter,
            Map<String, Object> autoSetMixinMap) throws FatalInitException

    {
        if (autoSetMixinMap == null)
        {
            autoSetMixinMap = new HashMap<>();
        }
        if (isMixin)
        {
            LOGGER.debug("***********For mixin:{}***********start:", clazz);
        } else
        {
            wholeClassCheckPassableGetter = new WholeClassCheckPassableGetterImpl();
        }

        InnerContextBridge innerContextBridge = autoSetHandle.getInnerContextBridge();
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(clazz, isMixin);
        if (portIn == null)
        {
            return null;
        }
        if (currentClassTied == null)
        {
            currentClassTied = portIn.getTiedName();
        }

        Porter porter = new Porter(autoSetHandle, wholeClassCheckPassableGetter);
        Map<String, PorterOfFun> childrenWithMethod = new HashMap<>();
        porter.childrenWithMethod = childrenWithMethod;

        porter.clazz = clazz;
        porter.object = object;
        porter.portIn = portIn;
        //自动设置,会确保接口对象已经实例化
        porter.doAutoSet(autoSetMixinMap);
        if (porter.object instanceof IPorter)
        {
            IPorter iPorter = (IPorter) porter.object;
            annotationDealt.setTiedName(porter.getPortIn(), iPorter.classTied());
        }


        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();

        //对MixinParser指定的类的Parser和Parser.parse的处理
        inObjDeal.sthUtil
                .bindParserAndParseWithMixin(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin);
        //对Parser和Parser.parse的处理
        inObjDeal.sthUtil.bindParserAndParse(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin);

        try
        {
            porter.inObj = inObjDeal.dealPortInObj(clazz, innerContextBridge);
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }

        wholeClassCheckPassableGetter.addAll(portIn.getCheckPassablesForWholeClass());

        /////处理混入接口------开始：
        //先处理混入接口，这样当前接口类的接口方法优先
        Class<?>[] mixins = SthUtil.getMixin(clazz);
        List<Porter> mixinList = new ArrayList<>(mixins.length);
        //List<Class<? extends CheckPassable>> mixinCheckForWholeClassList = new ArrayList<>();
        for (Class c : mixins)
        {
            if (!PortUtil.isPortClass(c))
            {
                continue;
            }
            Porter mixinPorter = porter(c, null, currentContextName, currentClassTied, autoSetHandle, true,
                    wholeClassCheckPassableGetter,
                    autoSetMixinMap);
            if (mixinPorter == null)
            {
                continue;
            }
            mixinList.add(mixinPorter);
            Map<String, PorterOfFun> mixinChildren = mixinPorter.childrenWithMethod;
            Iterator<PorterOfFun> mixinIt = mixinChildren.values().iterator();
            while (mixinIt.hasNext())
            {
                putFun(mixinIt.next(),porter.getObj(), childrenWithMethod, true, true);
            }
            mixinPorter.childrenWithMethod.clear();
            wholeClassCheckPassableGetter.addAll(mixinPorter.getPortIn().getCheckPassablesForWholeClass());
            autoSetHandle.addAutoSetThatOfMixin(porter.getObj(), mixinPorter.getObj());
        }
        if (mixinList.size() > 0)
        {
            porter.mixins = mixinList.toArray(new Porter[0]);
        }

        //实例化经检查对象并添加到map。
        inObjDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());

        if (isMixin)
        {
            LOGGER.debug("***********For mixin:{}***********end!", clazz);
        } else
        {
            wholeClassCheckPassableGetter.done();
            inObjDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps,
                    wholeClassCheckPassableGetter.getChecksForWholeClass());
        }
        ////////处理混入接口------结束


        List<_PortStart> portStarts = new ArrayList<>();
        List<_PortDestroy> portDestroys = new ArrayList<>();

        Method[] methods = WPTool.getAllPublicMethods(clazz);
        ObjectGetter objectGetter = () -> porter.getObj();
        for (Method method : methods)
        {

            if (mayAddStartOrDestroy(method, objectGetter, portStarts, portDestroys, annotationDealt))
            {
                method.setAccessible(true);
                continue;
            }
            backableSeek.push();
            PorterOfFun porterOfFun = porterOfFun(porter, method, innerContextBridge, backableSeek);
            backableSeek.pop();
            if (porterOfFun != null)
            {
                TiedType tiedType = TiedType.type(portIn.getTiedType(), porterOfFun.getMethodPortIn().getTiedType());
                annotationDealt.setTiedType(porterOfFun.getMethodPortIn(), tiedType);
                putFun(porterOfFun,porter.getObj(), childrenWithMethod, !isMixin, isMixin);
            }
        }

        _PortStart[] starts = portStarts.toArray(new _PortStart[0]);
        _PortDestroy[] destroys = portDestroys.toArray(new _PortDestroy[0]);
        Arrays.sort(starts);
        Arrays.sort(destroys);
        porter.starts = starts;
        porter.destroys = destroys;
        addPortAfterBefore(porter, currentContextName, currentClassTied, autoSetHandle);
        return porter;
    }

    private void putFun(PorterOfFun porterOfFun,Object finalObject, Map<String, PorterOfFun> childrenWithMethod, boolean willLog,
            boolean isMixin)
    {
        PorterOfFun lastFun = null;
        porterOfFun.finalObject=finalObject;
        TiedType tiedType = porterOfFun.getMethodPortIn().getTiedType();
        Method method = porterOfFun.getMethod();
        switch (tiedType)
        {

            case REST:
                lastFun = childrenWithMethod.put(porterOfFun.getMethodPortIn().getMethod().name(), porterOfFun);
                if (LOGGER.isDebugEnabled() && willLog)
                {
                    LOGGER.debug("add-rest:{} (outType={},function={}{})", porterOfFun.getMethodPortIn().getMethod(),
                            porterOfFun.getPortOut().getOutType(), method.getName(),
                            isMixin ? ",from " + method.getDeclaringClass() : "");
                }
                break;
            case DEFAULT:
                lastFun = childrenWithMethod
                        .put(porterOfFun.getMethodPortIn().getTiedName() + "/" + porterOfFun.getMethodPortIn()
                                .getMethod().name(), porterOfFun);
                if (LOGGER.isDebugEnabled() && willLog)
                {
                    LOGGER.debug("add:{},{} (outType={},function={}{})", porterOfFun.getMethodPortIn().getTiedName(),
                            porterOfFun.getMethodPortIn().getMethod(), porterOfFun.getPortOut().getOutType(),
                            method.getName(), isMixin ? ",from " + method.getDeclaringClass() : "");
                }
                break;
        }

        if (lastFun != null && LOGGER.isDebugEnabled())
        {
            LOGGER.debug("overrided:{}", lastFun.getMethod());
        }
    }

    private PorterOfFun porterOfFun(Porter porter, Method method, InnerContextBridge innerContextBridge,
            BackableSeek backableSeek)
    {
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(method, porter.getPortIn());
        if (portIn == null)
        {
            return null;
        }
        try
        {

            method.setAccessible(true);
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0]))
            {
                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
            }
            PorterOfFun porterOfFun = new PorterOfFun()
            {
                @Override
                public Object getObject()
                {
                    return porter.getObj();
                }
            };
            porterOfFun.method = method;
            porterOfFun.porter = porter;
            porterOfFun.portIn = portIn;
            porterOfFun.argCount = parameters.length;


            inObjDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());

            TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
            boolean hasBinded = SthUtil.bindParserAndParse(method, annotationDealt, portIn.getInNames(),
                    typeParserStore, backableSeek);

            if (!hasBinded)
            {
                //当函数上没有转换注解、而类上有时，加上此句是确保类上的转换对函数有想
                SthUtil.bindTypeParser(portIn.getInNames(), null, typeParserStore, backableSeek,
                        BackableSeek.SeekType.NotAdd_Bind);
            }

            porterOfFun.inObj = inObjDeal.dealPortInObj(method, innerContextBridge);

            porterOfFun.portOut = annotationDealt.portOut(method);

            return porterOfFun;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }

    }
}
