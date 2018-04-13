package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.sth.ObjectGetter;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterParamGetterImpl;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
@PortIn
@MixinOnly
public final class AnnotationDealt
{
    private boolean enableDefaultValue;


    private final Logger LOGGER;

    private AnnotationDealt(boolean enableDefaultValue)
    {
        this.enableDefaultValue = enableDefaultValue;
        LOGGER = LogUtil.logger(AnnotationDealt.class);
    }

    /**
     * @param enableDefaultValue 是否允许{@linkplain PortIn#value()}取默认值。
     */
    public static AnnotationDealt newInstance(boolean enableDefaultValue)
    {
        return new AnnotationDealt(enableDefaultValue);
    }


    public _SyncPorterOption syncPorterOption(Field field,
            PorterParamGetterImpl porterParamGetter) throws FatalInitException

    {
        if (field.isAnnotationPresent(SyncPorterOption.class))
        {
            _SyncPorterOption syncPorterOption = new _SyncPorterOption(porterParamGetter);
            SyncPorterOption option = field.getAnnotation(SyncPorterOption.class);
            String context = option.context().equals("") ? porterParamGetter.getContext() : option.context();
            String classTied;
            if (!SyncPorterOption.class.equals(option.porter()))
            {
                classTied = PortUtil.tied(option.porter());
            } else
            {
                classTied = option.classTied().equals("") ? porterParamGetter.getClassTied() : option.classTied();
            }


            String funTied = option.funTied().equals("") ? field.getName() : option.funTied();
            porterParamGetter.setContext(context);
            porterParamGetter.setClassTied(classTied);
            porterParamGetter.setFunTied(funTied);

            syncPorterOption.method = option.method();

            return syncPorterOption;

        } else
        {
            _SyncPorterOption syncPorterOption = new _SyncPorterOption(porterParamGetter);

            String context = porterParamGetter.getContext();
            String classTied = porterParamGetter.getClassTied();
            String funTied = field.getName();
            porterParamGetter.setContext(context);
            porterParamGetter.setClassTied(classTied);//用于检测名称是否合法
            porterParamGetter.setFunTied(funTied);
            syncPorterOption.method = PortMethod.GET;
            LOGGER.debug("Field[{}] not annotated with {}", field, SyncPorterOption.class.getName());
            return syncPorterOption;
        }

    }


    public _AutoSet autoSet(Field field)
    {
        AutoSet autoSet = field.getAnnotation(AutoSet.class);
        if (autoSet == null)
        {
            return null;
        }
        _AutoSet _autoSet = new _AutoSet();
        _autoSet.classValue = autoSet.classValue();
        _autoSet.value = autoSet.value();
        _autoSet.range = autoSet.range();
        return _autoSet;
    }

    public _Nece nece(Field field)
    {
        PortInObj.Nece nece = field.getAnnotation(PortInObj.Nece.class);
        if (nece == null)
        {
            return null;
        }
        _Nece _nece = new _Nece();
        if ("".equals(nece.value()))
        {
            _nece.value = field.getName();
        } else
        {
            _nece.value = nece.value();
        }
        _nece.toUnece = nece.toUnece();
        _nece.forMethods = nece.forMethods();
        _nece.forClassTieds = nece.forClassTieds();
        _nece.forFunTieds = nece.forFunTieds();

        _nece.init();
        return _nece;
    }

    public _UnNece unNece(Field field)
    {
        PortInObj.UnNece unNece = field.getAnnotation(PortInObj.UnNece.class);
        if (unNece == null)
        {
            return null;
        }
        _UnNece _unNece = new _UnNece();
        if ("".equals(unNece.value()))
        {
            _unNece.value = field.getName();
        } else
        {
            _unNece.value = unNece.value();
        }
        return _unNece;
    }

    public _PortInObj portInObj(Class<?> porterClass, Method method)
    {
        List<Class> classList = new ArrayList<>();

        PortInObj portInObj = AnnoUtil.getAnnotation(method, PortInObj.class);
        PortInObj.FromPorter fromPorter = AnnoUtil.getAnnotation(method, PortInObj.FromPorter.class);
        PortInObj.OnPorter onPorter = AnnoUtil.getAnnotation(porterClass, PortInObj.OnPorter.class);
        if (portInObj != null)
        {
            WPTool.addAll(classList, portInObj.value());
        }

        if (fromPorter != null)
        {
            if (onPorter == null)
            {

                if (fromPorter.value().length > 0)
                    LOGGER.warn("need @{}.{} in [{}] for method with @", PortInObj.class.getSimpleName(),
                            PortInObj.OnPorter.class.getSimpleName(),
                            porterClass.getName(), PortInObj.FromPorter.class.getSimpleName());
//                    throw new InitException("need @" + PortInObj.class.getSimpleName() + "." + PortInObj.OnPorter
// .class
//                            .getSimpleName() + " in [" + porterClass
//                            .getName() + "] for method with @" + PortInObj.FromPorter.class.getSimpleName());
            } else
            {
                Class<?>[] fclasses = fromPorter.value();
                Class<?>[] pclasses = onPorter.value();
                for (int i = 0; i < fclasses.length; i++)
                {
                    int index = -1;
                    int n = Integer.MAX_VALUE;
                    //寻找最亲的
                    for (int k = 0; k < pclasses.length; k++)
                    {
                        int _n = WPTool.subclassOf(pclasses[k], fclasses[i]);
                        if (_n >= 0 && _n < n)
                        {
                            n = _n;
                            index = k;
                        }
                    }
                    if (index == -1)
                    {
                        throw new InitException("not found [" + fclasses[i].getName() + "] in @" + PortInObj.class
                                .getSimpleName() + "." + PortInObj.OnPorter.class
                                .getSimpleName() + " in [" + porterClass
                                .getName() + "] for method with @" + PortInObj.FromPorter.class.getSimpleName());
                    } else
                    {
                        classList.add(pclasses[i]);
                    }
                }
            }
        }


        if (classList.size() == 0)
        {
            return null;
        }

        _PortInObj _portInObj = newPortInObj(classList.toArray(new Class[0]), method);
        return _portInObj;

    }


    private _PortInObj newPortInObj(Class<?>[] _classes, Method method)
    {
        _PortInObj.CLASS[] classes = new _PortInObj.CLASS[_classes.length];
        for (int i = 0; i < classes.length; i++)
        {
            Class<?> clazz = _classes[i];
            PortInObj.InObjDealt inObjDealt = AnnoUtil.getAnnotation(clazz, PortInObj.InObjDealt.class);
            if (inObjDealt != null)
            {
                Class<? extends PortInObj.IInObjHandle> handleClass = inObjDealt.handle();
                try
                {
                    classes[i] = new _PortInObj.CLASS(clazz, method, inObjDealt.option(),
                            WPTool.newObject(handleClass));
                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            } else
            {
                classes[i] = new _PortInObj.CLASS(clazz);
            }
        }

        _PortInObj _portInObj = new _PortInObj();
        _portInObj.value = classes;
        return _portInObj;
    }

    public _PortInObj portInObj(Class<?> clazz)
    {
        PortInObj portInObj = clazz.getAnnotation(PortInObj.class);
        if (portInObj == null)
        {
            return null;
        }
        _PortInObj _portInObj = newPortInObj(portInObj.value(), null);
        return _portInObj;
    }

    public _Parser parser(Method method)
    {
        return to_parser(AnnoUtil.getAnnotation(method, Parser.class));
    }

    public _Parser parser(Class<?> clazz)
    {
        return to_parser(clazz.getAnnotation(Parser.class));
    }

    private _Parser to_parser(Parser parser)
    {
        if (parser == null)
        {
            return null;
        }
        _Parser _parser = new _Parser();
        _parse[] ps = new _parse[parser.value().length];
        int i = 0;
        for (Parser.parse p : parser.value())
        {
            ps[i++] = to_parse(p);
        }
        _parser._parses = ps;
        return _parser;
    }

    public _parse parse(Method method)
    {
        return to_parse(AnnoUtil.getAnnotation(method, Parser.parse.class));
    }

    public _parse parse(Field field)
    {
        return to_parse(field.getAnnotation(Parser.parse.class));
    }

    private _parse to_parse(Parser.parse parse)
    {
        if (parse == null)
        {
            return null;
        }
        _parse _p = new _parse();
        _p.paramNames = parse.paramNames();
        _p.parserName = parse.parserName();
        _p.parserClass = parse.parser();
        return _p;
    }

    public _parse parse(Class<?> clazz)
    {
        return to_parse(clazz.getAnnotation(Parser.parse.class));
    }

    public _PortDestroy portDestroy(Method method, ObjectGetter objectGetter)
    {
        PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
        if (portDestroy == null)
        {
            return null;
        }
        _PortDestroy _portDestroy = new _PortDestroy();

        _portDestroy.porterOfFun = PorterOfFun.withMethodAndObject(method, objectGetter);
        _portDestroy.order = portDestroy.order();
        return _portDestroy;
    }


    public _PortStart portStart(Method method, @MayNull ObjectGetter objectGetter)
    {
        PortStart portStart = AnnoUtil.getAnnotation(method, PortStart.class);
        if (portStart == null)
        {
            return null;
        }
        _PortStart _portStart = new _PortStart();
        _portStart.porterOfFun = PorterOfFun.withMethodAndObject(method, objectGetter);
        _portStart.order = portStart.order();
        return _portStart;
    }

    public Method[] getPortStart(Object object)
    {
        ObjectGetter objectGetter = () -> object;

        Method[] methods = object.getClass().getMethods();
        List<_PortStart> list = new ArrayList<>();
        for (Method method : methods)
        {
            if (!Modifier.isAbstract(method.getModifiers()))
            {
                _PortStart portStart = portStart(method, objectGetter);
                if (portStart != null)
                {
                    list.add(portStart);
                }
            }
        }
        _PortStart[] portStarts = list.toArray(new _PortStart[0]);
        Arrays.sort(portStarts);
        Method[] starts = new Method[portStarts.length];
        for (int i = 0; i < portStarts.length; i++)
        {
            starts[i] = portStarts[i].porterOfFun.getMethod();
        }
        return starts;
    }

    public Method[] getPortDestroy(Object object)
    {
        ObjectGetter objectGetter = () -> object;

        Method[] methods = object.getClass().getMethods();
        List<_PortDestroy> list = new ArrayList<>();
        for (Method method : methods)
        {
            if (!Modifier.isAbstract(method.getModifiers()))
            {
                _PortDestroy portDestroy = portDestroy(method, objectGetter);
                if (portDestroy != null)
                {
                    list.add(portDestroy);
                }
            }
        }
        _PortDestroy[] portDestroys = list.toArray(new _PortDestroy[0]);
        Arrays.sort(portDestroys);
        Method[] destroys = new Method[portDestroys.length];
        for (int i = 0; i < portDestroys.length; i++)
        {
            destroys[i] = portDestroys[i].porterOfFun.getMethod();
        }
        return destroys;
    }

    public _PortOut portOut(Class<?> classPorter, OutType defaultPoutType)
    {
        _PortOut _portOut = new _PortOut();
        PortOut portOut = AnnoUtil.getAnnotation(classPorter, PortOut.class);
        if (portOut == null && defaultPoutType != null)
        {
            _portOut.outType = defaultPoutType;
        } else if (portOut != null)
        {
            _portOut.outType = portOut.value();
        } else
        {
            _portOut.outType = OutType.AUTO;
        }
        return _portOut;
    }

    public _PortOut portOut(Porter classPorter, Method method)
    {
        _PortOut _portOut = new _PortOut();
        PortOut portOut = AnnoUtil.getAnnotation(method, PortOut.class);

        if (portOut != null)
        {
            _portOut.outType = portOut.value();
        } else
        {
            if (method.getReturnType().equals(Void.TYPE))
            {
                _portOut.outType = OutType.VoidReturn;
            } else
            {
                _portOut.outType = OutType.AUTO;
            }
        }
        return _portOut;
    }


    public _PortIn portIn(Class<?> clazz)
    {
        return portIn(clazz, false);
    }

    public _PortIn portIn(Class<?> clazz, boolean isMixin)
    {
        PortIn portIn = clazz.getAnnotation(PortIn.class);
        if (portIn == null && isMixin)
        {
            portIn = AnnotationDealt.class.getAnnotation(PortIn.class);
        } else if (portIn == null || (!isMixin && AnnoUtil
                .isOneOfAnnotationsPresent(clazz, MixinOnly.class, MixinTo.class)))
        {
            return null;
        }
        _PortIn _portIn = new _PortIn(portIn.portFunType(), null, portIn.ignoredFunTieds(), portIn.enableMixinTo());
        _portIn.tiedNames = PortUtil.tieds(portIn, clazz, isMixin || enableDefaultValue);
        _portIn.inNames = InNames.fromStringArray(portIn.nece(), portIn.unece(), portIn.inner());
        _portIn.methods = new PortMethod[]{portIn.method()};
        _portIn.checks = portIn.checks();
        _portIn.checksForWholeClass = portIn.checksForWholeClass();
        _portIn.setTiedType(portIn.tiedType());
        _portIn.ignoreTypeParser = portIn.ignoreTypeParser();

        if (LOGGER.isDebugEnabled() && !isMixin)
        {
            LOGGER.debug("tiedName={},tiedType={},method={}", _portIn.tiedNames, _portIn.getTiedType(),
                    _portIn.methods);
        }

        return _portIn;
    }

    public _PortIn portIn(Method method, _PortIn class_PortIn)
    {
        _PortIn _portInOfMethod = null;
        PortIn portIn = AnnoUtil.getAnnotation(method, PortIn.class);
        if (portIn != null)
        {
//            Class<?>[] parameters = method.getParameterTypes();
//            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0]))
//            {
//                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
//            }

            PortFunType portFunType = PortFunType.type(class_PortIn.getPortFunType(), portIn.portFunType());
            _portInOfMethod = new _PortIn(portFunType, portIn.aspectOfClassPosition(), portIn.ignoredFunTieds(),
                    portIn.enableMixinTo());
            _portInOfMethod.setTiedType(TiedType.typeForFun(class_PortIn.getTiedType(), portIn.tiedType()));

            if (_portInOfMethod.getTiedType().isRest())
            {
                _portInOfMethod.tiedNames = new String[]{""};
            } else
            {
                _portInOfMethod.tiedNames = PortUtil
                        .tieds(portIn, method, enableDefaultValue);
            }


            _portInOfMethod.inNames = InNames.fromStringArray(portIn.nece(), portIn.unece(), portIn.inner());
            _portInOfMethod.checks = portIn.checks();
            _portInOfMethod.methods = AnnoUtil.methods(class_PortIn.getMethods()[0], portIn);
            _portInOfMethod.ignoreTypeParser = portIn.ignoreTypeParser();

        }
        return _portInOfMethod;
    }

    public void setClassTiedName(_PortIn portIn, String tiedName)
    {
        portIn.setTiedNames(new String[]{tiedName});
    }

    public void setTiedType(_PortIn portIn, TiedType tiedType)
    {
        portIn.setTiedType(tiedType);
    }
}
