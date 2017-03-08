package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public class SthDeal
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SthUtil.class);


    public SthDeal()
    {
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


    public Porter porter(Class<?> clazz, Object object, AutoSetUtil autoSetUtil) throws FatalInitException
    {
        return porter(clazz, object, autoSetUtil, false, null);
    }

    private Porter porter(Class<?> clazz, Object object, AutoSetUtil autoSetUtil,
            boolean isMixin, Map<String, Object> autoSetMixinMap) throws FatalInitException

    {
        if (autoSetMixinMap == null)
        {
            autoSetMixinMap = new HashMap<>();
        }
        if (isMixin)
        {
            LOGGER.debug("***********For mixin:{}***********start:", clazz);
        }

        InnerContextBridge innerContextBridge = autoSetUtil.getInnerContextBridge();
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(clazz, isMixin);
        if (portIn == null)
        {
            return null;
        }
        Porter porter = new Porter(autoSetUtil);
        Map<String, PorterOfFun> children = new HashMap<>();
        porter.children = children;

        porter.clazz = clazz;
        porter.object = object;
        porter.portIn = portIn;
        //自动设置
        porter.doAutoSet(autoSetMixinMap);
        if (porter.object instanceof IPorter)
        {
            IPorter iPorter = (IPorter) porter.object;
            porter.getPortIn().setTiedName(iPorter.classTied());
        }

        //实例化经检查对象并添加到map。
        SthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());

        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();

        //对MixinParser指定的类的Parser和Parser.parse的处理
        SthUtil.bindParserAndParseWithMixin(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin);
        //对Parser和Parser.parse的处理
        SthUtil.bindParserAndParse(clazz, innerContextBridge, portIn.getInNames(), backableSeek, !isMixin);

        try
        {
            porter.inObj = InObjDeal.dealPortInObj(clazz, innerContextBridge);
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }


        /////处理混入接口------开始：
        //先处理混入接口，这样当前接口类的接口方法优先
        Class<?>[] mixins = SthUtil.getMixin(clazz);
        List<Porter> mixinList = new ArrayList<>(mixins.length);
        for (Class c : mixins)
        {
            if (!PortUtil.isPortClass(c))
            {
                continue;
            }
            Porter mixinPorter = porter(c, null, autoSetUtil, true, autoSetMixinMap);
            if(mixinPorter==null){
                continue;
            }
            mixinList.add(mixinPorter);
            Map<String, PorterOfFun> mixinChildren = mixinPorter.children;
            Iterator<PorterOfFun> mixinIt = mixinChildren.values().iterator();
            while (mixinIt.hasNext())
            {
                putFun(mixinIt.next(), children, true, true);
            }
            mixinPorter.children.clear();
        }
        if (mixinList.size() > 0)
        {
            porter.mixins = mixinList.toArray(new Porter[0]);
        }

        if (isMixin)
        {
            LOGGER.debug("***********For mixin:{}***********end!", clazz);
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
                porterOfFun.getMethodPortIn().setTiedType(tiedType);
                putFun(porterOfFun, children, !isMixin, isMixin);
            }
        }

        _PortStart[] starts = portStarts.toArray(new _PortStart[0]);
        _PortDestroy[] destroys = portDestroys.toArray(new _PortDestroy[0]);
        Arrays.sort(starts);
        Arrays.sort(destroys);
        porter.starts = starts;
        porter.destroys = destroys;

        return porter;
    }

    private void putFun(PorterOfFun porterOfFun, Map<String, PorterOfFun> children, boolean willLog, boolean isMixin)
    {
        PorterOfFun lastFun = null;
        TiedType tiedType = porterOfFun.getMethodPortIn().getTiedType();
        Method method = porterOfFun.getMethod();
        switch (tiedType)
        {

            case REST:
                lastFun = children.put(porterOfFun.getMethodPortIn().getMethod().name(), porterOfFun);
                if (LOGGER.isDebugEnabled() && willLog)
                {
                    LOGGER.debug("add-rest:{} (outType={},function={}{})", porterOfFun.getMethodPortIn().getMethod(),
                            porterOfFun.getPortOut().getOutType(), method.getName(),
                            isMixin ? ",from " + method.getDeclaringClass() : "");
                }
                break;
            case Default:
                lastFun = children.put(porterOfFun.getMethodPortIn().getTiedName(), porterOfFun);
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


            SthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemps, portIn.getChecks());

            TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
            boolean hasBinded = SthUtil.bindParserAndParse(method, annotationDealt, portIn.getInNames(),
                    typeParserStore, backableSeek);

            if (!hasBinded)
            {
                //当函数上没有转换注解、而类上有时，加上此句是确保类上的转换对函数有想
                SthUtil.bindTypeParser(portIn.getInNames(), null, typeParserStore, backableSeek,
                        BackableSeek.SeekType.NotAdd_Bind);
            }

            porterOfFun.inObj = InObjDeal.dealPortInObj(method, innerContextBridge);

            porterOfFun.portOut = annotationDealt.portOut(method);

            return porterOfFun;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }

    }
}
