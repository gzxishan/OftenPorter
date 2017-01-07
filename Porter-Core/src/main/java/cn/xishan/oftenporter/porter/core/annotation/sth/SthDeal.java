package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public class SthDeal {
    private static final Logger LOGGER = LoggerFactory.getLogger(SthUtil.class);


    public SthDeal() {
    }


    private boolean mayAddStartOrDestroy(Method method, List<_PortStart> portStarts, List<_PortDestroy> portDestroys,
                                         AnnotationDealt annotationDealt) {
        _PortStart portStart = annotationDealt.portStart(method);
        if (portStart != null) {
            portStarts.add(portStart);
        }
        _PortDestroy portDestroy = annotationDealt.portDestroy(method);
        if (portDestroy != null) {
            portDestroys.add(portDestroy);
        }
        return portStart != null || portDestroy != null;
    }

    public Porter porter(Class<?> clazz, Object object, AutoSetUtil autoSetUtil) {
        InnerContextBridge innerContextBridge = autoSetUtil.getInnerContextBridge();
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(clazz);
        if (portIn == null) {
            return null;
        }
        Porter porter = new Porter(autoSetUtil);
        porter.clazz = clazz;
        porter.object = object;
        porter.portIn = portIn;
        //自动设置
        porter.doAutoSet();
        if (porter.getPortIn().newObjectWhenInit()) {
            porter.getObject();
        }
        //实例化经检查对象并添加到map。
        SthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemp, portIn.getChecks());

        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();

        //对Parser和Parser.parse的处理
        SthUtil.bindParserAndParse(clazz, innerContextBridge, portIn.getInNames(), backableSeek);

        try {
            porter.inObj = InObjDeal.dealPortInObj(clazz, innerContextBridge);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        Map<String, PorterOfFun> children = new HashMap<>();
        porter.children = children;
        List<_PortStart> portStarts = new ArrayList<>();
        List<_PortDestroy> portDestroys = new ArrayList<>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {

            if (mayAddStartOrDestroy(method, portStarts, portDestroys, annotationDealt)) {
                method.setAccessible(true);
                continue;
            }
            backableSeek.push();
            PorterOfFun porterOfFun = porterOfFun(porter, method, innerContextBridge, backableSeek);
            backableSeek.pop();
            if (porterOfFun != null) {
                TiedType tiedType = TiedType.type(portIn.getTiedType(), porterOfFun.getPortIn().getTiedType());
                porterOfFun.getPortIn().setTiedType(tiedType);
                switch (tiedType) {

                    case REST:
                        children.put(porterOfFun.getPortIn().getMethod().name(), porterOfFun);
                        LOGGER.debug("add-rest:{} (function={})", porterOfFun.getPortIn().getMethod(),
                                     method.getName());
                        break;
                    case Default:
                        children.put(porterOfFun.getPortIn().getTiedName(), porterOfFun);
                        LOGGER.debug("add:{},{} (function={})", porterOfFun.getPortIn().getTiedName(),
                                     porterOfFun.getPortIn().getMethod(), method.getName());
                        break;
                }

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

    private PorterOfFun porterOfFun(Porter porter, Method method, InnerContextBridge innerContextBridge,
                                    BackableSeek backableSeek) {
        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        _PortIn portIn = annotationDealt.portIn(method, porter.getPortIn());
        if (portIn == null) {
            return null;
        }
        try {

            method.setAccessible(true);
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0])) {
                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
            }
            PorterOfFun porterOfFun = new PorterOfFun();
            porterOfFun.method = method;
            porterOfFun.portIn = portIn;
            porterOfFun.argCount = parameters.length;


            SthUtil.addCheckPassable(innerContextBridge.checkPassableForCFTemp, portIn.getChecks());

            TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
            boolean hasBinded = SthUtil.bindParserAndParse(method, annotationDealt, portIn.getInNames(),
                                                           typeParserStore, backableSeek);

            if (!hasBinded) {
                //当函数上没有转换注解、而类上有时，加上此句是确保类上的转换对函数有想
                SthUtil.bindTypeParser(portIn.getInNames(), null, typeParserStore, backableSeek,
                                       BackableSeek.SeekType.NotAdd_Bind);
            }

            porterOfFun.inObj = InObjDeal.dealPortInObj(method, innerContextBridge);

            porterOfFun.portOut = annotationDealt.portOut(method);

            return porterOfFun;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

    }
}
