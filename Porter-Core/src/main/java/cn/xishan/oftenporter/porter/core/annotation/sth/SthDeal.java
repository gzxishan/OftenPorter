package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.PortExecutor;
import cn.xishan.oftenporter.porter.core.annotation.PortInObjBind;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
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
            currentClassTied = portIn.getTiedNames()[0];
        }

        Porter porter = new Porter(clazz, autoSetHandle, wholeClassCheckPassableGetter);
        Map<String, PorterOfFun> childrenWithMethod = new HashMap<>();
        porter.childrenWithMethod = childrenWithMethod;

        porter.object = object;
        porter.portIn = portIn;
        //自动设置,会确保接口对象已经实例化
        porter.doAutoSet(autoSetMixinMap);
        porter.finalObject = porter.getObj();
        if (porter.object instanceof IPorter)
        {
            IPorter iPorter = (IPorter) porter.object;
            annotationDealt.setClassTiedName(porter.getPortIn(), iPorter.classTied());
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
                putFun(mixinIt.next(), childrenWithMethod, true, true);
            }
            mixinPorter.finalObject = porter.getObj();
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
//                TiedType tiedType = TiedType.type(portIn.getTiedType(), porterOfFun.getMethodPortIn().getTiedType());
//                //设置方法的TiedType
//                annotationDealt.setTiedType(porterOfFun.getMethodPortIn(), tiedType);
                putFun(porterOfFun, childrenWithMethod, !isMixin, isMixin);
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

    private void putFun(PorterOfFun porterOfFun, Map<String, PorterOfFun> childrenWithMethod, boolean willLog,
            boolean isMixin)
    {
        PorterOfFun lastFun = null;
        TiedType tiedType = porterOfFun.getMethodPortIn().getTiedType();
        Method method = porterOfFun.getMethod();


        switch (tiedType)
        {

            case REST:
            case FORCE_REST:
            {
                PortMethod[] portMethods = porterOfFun.getMethodPortIn().getMethods();
                for (PortMethod portMethod : portMethods)
                {
                    lastFun = childrenWithMethod.put(portMethod.name(), porterOfFun);
                    if (LOGGER.isDebugEnabled() && willLog)
                    {
                        LOGGER.debug("add-rest:{} (outType={},function={}{})",
                                portMethod, porterOfFun.getPortOut().getOutType(), method.getName(),
                                isMixin ? ",from " + method.getDeclaringClass() : "");
                    }
                    if (lastFun != null && LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("overrided:{}", lastFun.getMethod());
                    }
                }
            }

            break;
            case DEFAULT:
                PortMethod[] portMethods = porterOfFun.getMethodPortIn().getMethods();
                for (PortMethod portMethod : portMethods)
                {
                    String[] tieds = porterOfFun.getMethodPortIn().getTiedNames();
                    String[] ignoredFunTieds = StrUtil
                            .newArray(porterOfFun.getPorter().getPortIn().getIgnoredFunTieds());
                    Arrays.sort(ignoredFunTieds);
                    for (String tiedName : tieds)
                    {
                        if(Arrays.binarySearch(ignoredFunTieds,tiedName)>=0){
                            if (LOGGER.isDebugEnabled() && willLog)
                            {
                                LOGGER.debug("ignore:{},{} (outType={},function={}{})",
                                        tiedName, portMethod, porterOfFun.getPortOut().getOutType(),
                                        method.getName(), isMixin ? ",from " + method.getDeclaringClass() : "");
                            }
                            continue;
                        }
                        lastFun = childrenWithMethod
                                .put(tiedName + "/" + portMethod.name(), porterOfFun);
                        if (LOGGER.isDebugEnabled() && willLog)
                        {
                            LOGGER.debug("add:{},{} (outType={},function={}{})",
                                    tiedName, portMethod, porterOfFun.getPortOut().getOutType(),
                                    method.getName(), isMixin ? ",from " + method.getDeclaringClass() : "");
                        }

                        if (lastFun != null && LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("overrided:{}", lastFun.getMethod());
                        }
                    }
                }

                break;
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

            porterOfFun.inObj = inObjDeal
                    .dealPortInObj(porter.getClazz().getAnnotation(PortInObjBind.ObjList.class), method,
                            innerContextBridge);

            porterOfFun.portOut = annotationDealt.portOut(method);

            return porterOfFun;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }

    }

    public static SyncPorter newSyncPorter(_SyncPorterOption syncPorterOption)
    {
        SyncPorterImpl syncPorter = new SyncPorterImpl(syncPorterOption);
        return syncPorter;
    }
}
