package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.ContextPorter;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
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
    private OPEntitiesDeal OPEntitiesDeal;

    public SthDeal()
    {
        LOGGER = LogUtil.logger(SthDeal.class);
        OPEntitiesDeal = new OPEntitiesDeal();
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
            Map<Class, Set<_MixinPorter>> mixinToMap,
            String currentContextName,
            AutoSetHandle autoSetHandle) throws FatalInitException
    {
        return porter(srcPorter, mixinToMap, currentContextName, null, autoSetHandle, false, null);
    }


    /**
     * <pre>
     * 1.会处理{@linkplain AspectOperationOfPortIn}
     *
     * </pre>
     */
    private Porter porter(ContextPorter.SrcPorter srcPorter, Map<Class, Set<_MixinPorter>> mixinToMap,
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


        Porter porter = new Porter(clazz, autoSetHandle, wholeClassCheckPassableGetter,
                autoSetHandle.getArgumentsFactory());
        Map<String, PorterOfFun> childrenWithMethod = new HashMap<>();
        porter.childrenWithMethod = childrenWithMethod;

        porter.portOut = annotationDealt.portOut(clazz,
                autoSetHandle.getInnerContextBridge().defaultOutType);
        porter.object = srcPorter.getObject();
        porter.portIn = portIn;
        //自动设置,会确保接口对象已经实例化
        porter.addAutoSet();
        if (porter.object instanceof IPorter)
        {
            IPorter iPorter = (IPorter) porter.object;
            annotationDealt.setClassTiedName(porter.getPortIn(), iPorter.classTied());
        }

        if (currentClassTied == null)
        {
            currentClassTied = portIn.getTiedNames()[0];
        }

        if (LOGGER.isDebugEnabled() && !isMixin)
        {
            LOGGER.debug("tiedName={},tiedType={},method={}", portIn.getTiedNames(), portIn.getTiedType(),
                    portIn.getMethods());
        }

        //处理类上的AspectFunOperation
        List<AspectOperationOfPortIn.Handle> classHandles = seekAspectFunOperation(autoSetHandle,
                AnnoUtil.Advanced.getAnnotationsForAspectOperationOfPortIn(porter), porter,
                null, null);

        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();

        //对MixinParse指定的类的Parse的处理
        OPEntitiesDeal.sthUtil
                .bindParsesWithMixin(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin,
                        mixinToMap);
        //对Parse的处理
        OPEntitiesDeal.sthUtil
                .bindParses(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin, mixinToMap);

        try
        {
            porter.opEntities = OPEntitiesDeal.dealOPEntities(clazz, innerContextBridge, autoSetHandle);
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }

        wholeClassCheckPassableGetter.addAll(portIn.getCheckPassablesForWholeClass());


        List<_PortStart> portStarts = new ArrayList<>();
        List<_PortDestroy> portDestroys = new ArrayList<>();

        /////处理自身接口----开始
        Method[] methods = WPTool.getAllMethods(clazz);
        ObjectGetter objectGetter = porter::getObj;
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

                //扫描AspectFunOperation
                seekAspectFunOperation(autoSetHandle, porterOfFun, classHandles);

                putFun(porterOfFun, childrenWithMethod, !isMixin, isMixin, !isMixin);
            }
        }
        //处理自身接口----结束


        /////处理混入接口------开始：
        //后处理混入接口，这样可以控制override
        _MixinPorter[] mixins = SthUtil.getMixin(clazz, mixinToMap);
        List<Porter> mixinList = new ArrayList<>(mixins.length);
        //List<Class<? extends CheckPassable>> mixinCheckForWholeClassList = new ArrayList<>();
        for (_MixinPorter _mixinPorter : mixins)
        {
            if (!PortUtil.isMixinPortClass(_mixinPorter.getClazz()))
            {
                continue;
            }
            Porter mixinPorter = porter(new ContextPorter.SrcPorter(_mixinPorter.getClazz(), _mixinPorter.getObject()),
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
                putFun(mixinIt.next(), childrenWithMethod, true, true, _mixinPorter.isOverride());
            }
            mixinPorter.finalObject = porter.getFinalPorterObject();
            mixinPorter.finalPorter = porter.getFinalPorter();
            mixinPorter.childrenWithMethod.clear();
            wholeClassCheckPassableGetter.addAll(mixinPorter.getPortIn().getCheckPassablesForWholeClass());
            autoSetHandle.addAutoSetThatOfMixin(porter.getObj(), mixinPorter.getObj());
        }
        if (mixinList.size() > 0)
        {
            porter.mixins = mixinList.toArray(new Porter[0]);
        }

        //实例化经检查对象并添加到map。
        OPEntitiesDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());

        if (!isMixin)
        {
            wholeClassCheckPassableGetter.done();
            OPEntitiesDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps,
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
            List<AspectOperationOfPortIn.Handle> classHandles)
    {
        Annotation[] annotations = AnnoUtil.Advanced.getAnnotationsForAspectOperationOfPortIn(porterOfFun);
        List<AspectOperationOfPortIn.Handle> handles = seekAspectFunOperation(setHandle, annotations, porterOfFun,
                classHandles, porterOfFun.getMethodPortIn().getAspectPosition());
        if (handles.size() > 0)
        {
            porterOfFun.setHandles(handles.toArray(new AspectOperationOfPortIn.Handle[0]));
        }
    }


    /**
     * @param annotations
     * @param object
     * @param _handles
     * @return 返回一个新的List
     */
    private List<AspectOperationOfPortIn.Handle> seekAspectFunOperation(AutoSetHandle setHandle,
            Annotation[] annotations,
            Object object,
            List<AspectOperationOfPortIn.Handle> _handles, AspectPosition aspectPosition)
    {

        List<AspectOperationOfPortIn.Handle> handles = new ArrayList<>();
        AnnotationDealt annotationDealt = setHandle.getInnerContextBridge().annotationDealt;

        if (_handles != null && aspectPosition == AspectPosition.BEFORE)
        {
            handles.addAll(_handles);
        }

        IConfigData configData = setHandle.getContextObject(IConfigData.class);
        for (Annotation annotation : annotations)
        {
            AspectOperationOfPortIn aspectOperationOfPortIn = AnnoUtil.Advanced.getAspectOperationOfPortIn(annotation);
            if (aspectOperationOfPortIn == null)
            {
                continue;
            }

            try
            {
                AspectOperationOfPortIn.Handle handle = WPTool.newObject(aspectOperationOfPortIn.handle());
                if (object instanceof PorterOfFun)
                {
                    PorterOfFun porterOfFun = (PorterOfFun) object;
                    if (handle.init(annotation, configData, porterOfFun))
                    {
                        porterOfFun.portOut._setOutType(handle.getOutType());
                        porterOfFun.portIn.setPortFunType(handle.getPortFunType());
                        PortMethod[] portMethods = handle.getMethods();
                        if (portMethods != null && portMethods.length > 0)
                        {
                            portMethods = AnnoUtil.methods(porterOfFun.getFinalPorter().portIn.getMethods()[0],
                                    null, portMethods);
                            annotationDealt.setMethods(porterOfFun.portIn, portMethods);
                        }

                        annotationDealt.setTiedType(porterOfFun.portIn,
                                TiedType.typeForFun(porterOfFun.portIn.getTiedType(), handle.getTiedType()));
                        handles.add(handle);
                        setHandle.addAutoSetsForNotPorter(new Object[]{handle});
                    }
                } else
                {
                    Porter porter = (Porter) object;
                    if (handle.init(annotation, configData, porter))
                    {
                        porter.portOut._setOutType(handle.getOutType());
                        porter.portIn.setPortFunType(handle.getPortFunType());
                        annotationDealt.setTiedType(porter.portIn,
                                TiedType.typeForFun(porter.portIn.getTiedType(), handle.getTiedType()));
                        handles.add(handle);
                        setHandle.addAutoSetsForNotPorter(new Object[]{handle});
                    }
                }

            } catch (Exception e)
            {
                throw new InitException(e);
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
        _PortIn portIn = annotationDealt.portIn(porter, method);
        PorterOfFun porterOfFun = null;
        if (portIn != null)
        {
            try
            {
                method.setAccessible(true);
                Class<?>[] parameters = method.getParameterTypes();

                porterOfFun = new PorterOfFun(method)
                {
                    @Override
                    public Object getObject()
                    {
                        return porter.getObj();
                    }
                };
                porterOfFun.porter = porter;
                porterOfFun.portIn = portIn;
                porterOfFun.argCount = parameters.length;

                OPEntitiesDeal.sthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());
                TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
                boolean hasBinded = SthUtil.bindParses(method, annotationDealt, portIn.getInNames(), typeParserStore,
                        backableSeek);

                if (!hasBinded)
                {
                    //当函数上没有转换注解、而类上有时，加上此句是确保类上的转换对函数有想
                    SthUtil.bindTypeParse(portIn.getInNames(), null, typeParserStore, backableSeek,
                            BackableSeek.SeekType.NotAdd_Bind);
                }
                porterOfFun.opEntities = OPEntitiesDeal
                        .dealOPEntities(porter.getClazz(), method, innerContextBridge, autoSetHandle);
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
