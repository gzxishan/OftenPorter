package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.ContextPorter;
import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
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


    public Porter porter(ContextPorter.SrcPorter srcPorter,
            Map<Class, Set<_MinxinPorter>> mixinToMap,
            String currentContextName,
            AutoSetHandle autoSetHandle) throws FatalInitException
    {
        return porter(srcPorter, mixinToMap, currentContextName, null, autoSetHandle, false, null);
    }


    /**
     * <pre>
     * 1.会处理{@linkplain AspectFunOperation}
     *
     * </pre>
     */
    private Porter porter(ContextPorter.SrcPorter srcPorter, Map<Class, Set<_MinxinPorter>> mixinToMap,
            String currentContextName, String currentClassTied,
            AutoSetHandle autoSetHandle,
            boolean isMixin, WholeClassCheckPassableGetterImpl wholeClassCheckPassableGetter) throws FatalInitException

    {
        Class clazz = srcPorter.getClazz();
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

        porter.portOut = annotationDealt.portOut(clazz,
                autoSetHandle.getInnerContextBridge().defaultOutType);
        porter.object = srcPorter.getObject();
        porter.portIn = portIn;
        //自动设置,会确保接口对象已经实例化
        porter.addAutoSet();
        porter.finalObject = porter.getObj();
        if (porter.object instanceof IPorter)
        {
            IPorter iPorter = (IPorter) porter.object;
            annotationDealt.setClassTiedName(porter.getPortIn(), iPorter.classTied());
        }
        //处理类上的AspectFunOperation
        List<AspectFunOperation.Handle> classHandles = seekAspectFunOperation(autoSetHandle,
                clazz.getDeclaredAnnotations(), porter,
                null, null);

        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();

        //对MixinParser指定的类的Parser和Parser.parse的处理
        inObjDeal.sthUtil
                .bindParserAndParseWithMixin(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin,
                        mixinToMap);
        //对Parser和Parser.parse的处理
        inObjDeal.sthUtil
                .bindParserAndParse(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin, mixinToMap);

        try
        {
            porter.inObj = inObjDeal.dealPortInObj(clazz, innerContextBridge, autoSetHandle);
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }

        wholeClassCheckPassableGetter.addAll(portIn.getCheckPassablesForWholeClass());


        List<_PortStart> portStarts = new ArrayList<>();
        List<_PortDestroy> portDestroys = new ArrayList<>();

        /////处理自身接口----开始
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
            PorterOfFun porterOfFun = porterOfFun(porter, method, innerContextBridge, backableSeek, autoSetHandle);
            backableSeek.pop();
            if (porterOfFun != null)
            {
//                TiedType tiedType = TiedType.type(portIn.getTiedType(), porterOfFun.getMethodPortIn().getTiedType());
//                //设置方法的TiedType
//                annotationDealt.setTiedType(porterOfFun.getMethodPortIn(), tiedType);
                putFun(porterOfFun, childrenWithMethod, !isMixin, isMixin, !isMixin);

                //扫描AspectFunOperation
                seekAspectFunOperation(autoSetHandle, porterOfFun, classHandles);
            }
        }
        //处理自身接口----结束


        /////处理混入接口------开始：
        //后处理混入接口，这样可以控制override
        _MinxinPorter[] mixins = SthUtil.getMixin(clazz, mixinToMap);
        List<Porter> mixinList = new ArrayList<>(mixins.length);
        //List<Class<? extends CheckPassable>> mixinCheckForWholeClassList = new ArrayList<>();
        for (_MinxinPorter minxinPorter : mixins)
        {
            if (!PortUtil.isMixinPortClass(minxinPorter.getClazz()))
            {
                continue;
            }
            Porter mixinPorter = porter(new ContextPorter.SrcPorter(minxinPorter.getClazz(), minxinPorter.getObject()),
                    mixinToMap, currentContextName, currentClassTied,
                    autoSetHandle, true, wholeClassCheckPassableGetter);
            if (mixinPorter == null)
            {
                continue;
            }
            mixinList.add(mixinPorter);
            Map<String, PorterOfFun> mixinChildren = mixinPorter.childrenWithMethod;
            Iterator<PorterOfFun> mixinIt = mixinChildren.values().iterator();
            while (mixinIt.hasNext())
            {
                putFun(mixinIt.next(), childrenWithMethod, true, true, minxinPorter.isOverride());
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

        _PortStart[] starts = portStarts.toArray(new _PortStart[0]);
        _PortDestroy[] destroys = portDestroys.toArray(new _PortDestroy[0]);
        Arrays.sort(starts);
        Arrays.sort(destroys);
        porter.starts = starts;
        porter.destroys = destroys;
        return porter;
    }

    private void seekAspectFunOperation(AutoSetHandle setHandle, PorterOfFun porterOfFun,
            List<AspectFunOperation.Handle> classHandles)
    {
        Annotation[] annotations = porterOfFun.getMethod().getDeclaredAnnotations();
        List<AspectFunOperation.Handle> handles = seekAspectFunOperation(setHandle, annotations, porterOfFun,
                classHandles, porterOfFun.getMethodPortIn().getAspectPosition());
        if (handles.size() > 0)
        {
            porterOfFun.setHandles(handles.toArray(new AspectFunOperation.Handle[0]));
        }
    }


    /**
     * @param annotations
     * @param object
     * @param _handles
     * @return 返回一个新的List
     */
    private List<AspectFunOperation.Handle> seekAspectFunOperation(AutoSetHandle setHandle, Annotation[] annotations,
            Object object,
            List<AspectFunOperation.Handle> _handles, AspectPosition aspectPosition)
    {
        // Annotation[] annotations = porterOfFun.getMethod().getDeclaredAnnotations();

        List<AspectFunOperation.Handle> handles = new ArrayList<>();

        if (_handles != null && aspectPosition == AspectPosition.BEFORE)
        {
            handles.addAll(_handles);
        }

        for (Annotation annotation : annotations)
        {
            Class<? extends Annotation> atype = annotation.annotationType();
            if (!atype.isAnnotationPresent(AspectFunOperation.class))
            {
                continue;
            }
            AspectFunOperation aspectFunOperation = AnnoUtil
                    .getAnnotation(atype, AspectFunOperation.class);
            if (aspectFunOperation != null)
            {
                try
                {
                    AspectFunOperation.Handle handle = WPTool.newObject(aspectFunOperation.handle());
                    if (object instanceof PorterOfFun)
                    {
                        PorterOfFun porterOfFun = (PorterOfFun) object;
                        if (handle.init(annotation, porterOfFun))
                        {
                            if (handle.getOutType() != null)
                            {
                                porterOfFun.portOut._setOutType(handle.getOutType());
                            }
                            handles.add(handle);
                            setHandle.addAutoSetsForNotPorter(new Object[]{handle});
                        }
                    } else
                    {
                        Porter porter = (Porter) object;
                        if (handle.init(annotation, porter))
                        {
                            if (handle.getOutType() != null)
                            {
                                porter.portOut._setOutType(handle.getOutType());
                            }
                            handles.add(handle);
                            setHandle.addAutoSetsForNotPorter(new Object[]{handle});
                        }
                    }

                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            }
        }

        if (_handles != null && aspectPosition == AspectPosition.AFTER)
        {
            handles.addAll(_handles);
        }

        return handles;
    }

    private void putFun(PorterOfFun porterOfFun, Map<String, PorterOfFun> childrenWithMethod, boolean willLog,
            boolean isMixin, boolean override)
    {
        PorterOfFun lastFun;
        TiedType tiedType = porterOfFun.getMethodPortIn().getTiedType();
        Method method = porterOfFun.getMethod();

        PortFunType portFunType = porterOfFun.getMethodPortIn().getPortFunType();
        switch (tiedType)
        {

            case REST:
            case FORCE_REST:
            {
                PortMethod[] portMethods = porterOfFun.getMethodPortIn().getMethods();
                for (PortMethod portMethod : portMethods)
                {
                    String key = portMethod.name();
                    if (!childrenWithMethod.containsKey(key) || override)
                    {
                        lastFun = childrenWithMethod.put(key, porterOfFun);
                        if (LOGGER.isDebugEnabled() && willLog)
                        {
                            LOGGER.debug("add-rest:{} (outType={},portFunType={},function={}{})",
                                    portMethod, porterOfFun.getPortOut().getOutType(), portFunType, method.getName(),
                                    isMixin ? ",from " + method.getDeclaringClass() : "");
                        }
                        if (lastFun != null && LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("overrided:{}", lastFun.getMethod());
                        }
                    } else
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("ignore:{}", porterOfFun.getMethod());
                        }
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
                        if (Arrays.binarySearch(ignoredFunTieds, tiedName) >= 0)
                        {
                            if (LOGGER.isDebugEnabled() && willLog)
                            {
                                LOGGER.debug("ignore:{},{} (outType={},portFunType={},jmethod={}{})",
                                        tiedName, portMethod, porterOfFun.getPortOut().getOutType(), portFunType,
                                        method.getName(), isMixin ? ",from " + method.getDeclaringClass() : "");
                            }
                            continue;
                        }
                        String key = tiedName + Porter.TIED_KEY_SEPARATOR + portMethod.name();
                        if (!childrenWithMethod.containsKey(key) || override)
                        {
                            lastFun = childrenWithMethod.put(key, porterOfFun);
                            if (LOGGER.isDebugEnabled() && willLog)
                            {
                                LOGGER.debug("add:{},{} (outType={},portFunType={},jmethod={}{})",
                                        tiedName, portMethod, porterOfFun.getPortOut().getOutType(), portFunType,
                                        method.getName(), isMixin ? ",from " + method.getDeclaringClass() : "");
                            }

                            if (lastFun != null && LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug("overrided:{}", lastFun.getMethod());
                            }
                        } else
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug("ignore:{}", porterOfFun.getMethod());
                            }
                        }

                    }
                }

                break;
        }


    }

    private PorterOfFun porterOfFun(Porter porter, Method method, InnerContextBridge innerContextBridge,
            BackableSeek backableSeek, AutoSetHandle autoSetHandle)
    {
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(method, porter.getPortIn());
        PorterOfFun porterOfFun = null;
        if (portIn != null)
        {
            try
            {
                method.setAccessible(true);
                Class<?>[] parameters = method.getParameterTypes();
//            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0]))
//            {
//                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
//            }
                porterOfFun = new PorterOfFun()
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
                boolean hasBinded = SthUtil
                        .bindParserAndParse(method, annotationDealt, portIn.getInNames(), typeParserStore,
                                backableSeek);

                if (!hasBinded)
                {
                    //当函数上没有转换注解、而类上有时，加上此句是确保类上的转换对函数有想
                    SthUtil.bindTypeParser(portIn.getInNames(), null, typeParserStore, backableSeek,
                            BackableSeek.SeekType.NotAdd_Bind);
                }
                porterOfFun.inObj = inObjDeal
                        .dealPortInObj(porter.getClazz(), method, innerContextBridge, autoSetHandle);
                porterOfFun.portOut = annotationDealt.portOut(porter, method);
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
                porterOfFun = null;
            }
        }
        return porterOfFun;

    }

    public static SyncPorter newSyncPorter(_SyncPorterOption syncPorterOption, boolean isInner, Delivery delivery)
    {
        SyncPorterImpl syncPorter = new SyncPorterImpl(syncPorterOption, isInner);
        syncPorter.delivery = delivery;
        return syncPorter;
    }
}
