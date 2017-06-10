package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.sth.ObjectGetter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterParamGetterImpl;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
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

    public List<_PortFilterOne> portAfters(Class<?> clazz, String currentContext, String currentClassTied)
    {
        List<_PortFilterOne> list = new ArrayList<>();
        if (clazz.isAnnotationPresent(PortIn.After.class))
        {
            PortIn.After after = clazz.getAnnotation(PortIn.After.class);
            list.add(portAfter(after, currentContext, currentClassTied));
        }
        if (clazz.isAnnotationPresent(PortIn.Filter.class))
        {
            PortIn.Filter filter = clazz.getAnnotation(PortIn.Filter.class);
            for (PortIn.After after : filter.after())
            {
                list.add(portAfter(after, currentContext, currentClassTied));
            }
        }
        return list;
    }

    public List<_PortFilterOne> portAfters(Method method, String currentContext, String currentClassTied)
    {
        List<_PortFilterOne> list = new ArrayList<>();
        if (method.isAnnotationPresent(PortIn.After.class))
        {
            PortIn.After after = method.getAnnotation(PortIn.After.class);
            list.add(portAfter(after, currentContext, currentClassTied));
        }
        if (method.isAnnotationPresent(PortIn.Filter.class))
        {
            PortIn.Filter filter = method.getAnnotation(PortIn.Filter.class);
            for (PortIn.After after : filter.after())
            {
                list.add(portAfter(after, currentContext, currentClassTied));
            }
        }
        return list;
    }

    public _SyncPorterOption syncPorterOption(Field field,
            PorterParamGetterImpl porterParamGetter) throws FatalInitException

    {
        if (field.isAnnotationPresent(SyncPorterOption.class))
        {
            _SyncPorterOption syncPorterOption = new _SyncPorterOption(porterParamGetter);
            SyncPorterOption option = field.getAnnotation(SyncPorterOption.class);
            String context = option.context().equals("") ? porterParamGetter.getContext() : option.context();
            String classTied = option.classTied().equals("") ? porterParamGetter.getClassTied() : option.classTied();
            porterParamGetter.setContext(context);
            porterParamGetter.setClassTied(classTied);
            porterParamGetter.setFunTied(option.funTied());

            syncPorterOption.method = option.method();

            return syncPorterOption;

        } else
        {
            throw new FatalInitException(
                    String.format("Field[%s] not annotated with %s", field, SyncPorterOption.class.getName()));
        }

    }

    private _PortFilterOne portAfter(PortIn.After after, String currentContext, String currentClassTied)
    {
        String context = after.context().equals("") ? currentContext : after.context();
        String classTied = after.classTied().equals("") ? currentClassTied : after.classTied();
        PortUtil.checkName(context);
        PortUtil.checkName(classTied);
        PortUtil.checkName(after.funTied());
        _PortFilterOne portAfter = new _PortFilterOne(after.method());
        portAfter.pathWithContext = "/" + context + "/" + classTied + "/" + after.funTied();
        return portAfter;
    }

    public List<_PortFilterOne> portBefores(Class<?> clazz, String currentContext, String currentClassTied)
    {
        List<_PortFilterOne> list = new ArrayList<>();
        if (clazz.isAnnotationPresent(PortIn.Before.class))
        {
            PortIn.Before before = clazz.getAnnotation(PortIn.Before.class);
            list.add(portBefore(before, currentContext, currentClassTied));
        }
        if (clazz.isAnnotationPresent(PortIn.Filter.class))
        {
            PortIn.Filter filter = clazz.getAnnotation(PortIn.Filter.class);
            for (PortIn.Before before : filter.before())
            {
                list.add(portBefore(before, currentContext, currentClassTied));
            }
        }
        return list;
    }

    public List<_PortFilterOne> portBefores(Method method, String currentContext, String currentClassTied)
    {
        List<_PortFilterOne> list = new ArrayList<>();
        if (method.isAnnotationPresent(PortIn.Before.class))
        {
            PortIn.Before before = method.getAnnotation(PortIn.Before.class);
            list.add(portBefore(before, currentContext, currentClassTied));
        }
        if (method.isAnnotationPresent(PortIn.Filter.class))
        {
            PortIn.Filter filter = method.getAnnotation(PortIn.Filter.class);
            for (PortIn.Before before : filter.before())
            {
                list.add(portBefore(before, currentContext, currentClassTied));
            }
        }
        return list;
    }

    private _PortFilterOne portBefore(PortIn.Before before, String currentContext, String currentClassTied)
    {
        String context = before.context().equals("") ? currentContext : before.context();
        String classTied = before.classTied().equals("") ? currentClassTied : before.classTied();
        PortUtil.checkName(context);
        PortUtil.checkName(classTied);
        PortUtil.checkName(before.funTied());

        _PortFilterOne portBefore = new _PortFilterOne(before.method());
        portBefore.pathWithContext = "/" + context + "/" + classTied + "/" + before.funTied();
        return portBefore;
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

    public _PortInObj portInObj(Method method)
    {
        return to_PortInObj(AnnoUtil.getAnnotation(method, PortInObj.class));
    }

    public _PortInObj portInObj(Class<?> clazz)
    {
        return to_PortInObj(clazz.getAnnotation(PortInObj.class));
    }

    private _PortInObj to_PortInObj(PortInObj portInObj)
    {
        if (portInObj == null)
        {
            return null;
        }
        _PortInObj _portInObj = new _PortInObj();
        _portInObj.value = portInObj.value();
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

    public _PortStart portStart(Method method, ObjectGetter objectGetter)
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

    public _PortOut portOut(Method method)
    {
        _PortOut _portOut = new _PortOut();
        PortOut portOut = AnnoUtil.getAnnotation(method, PortOut.class);
        if (method.getReturnType().equals(Void.TYPE))
        {
            _portOut.outType = OutType.NO_RESPONSE;
        } else if (portOut != null)
        {
            _portOut.outType = portOut.value();
        } else
        {
            _portOut.outType = OutType.AUTO;
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
        if (portIn == null || (!isMixin && clazz.isAnnotationPresent(PortIn.MinxinOnly.class)))
        {
            return null;
        }
        _PortIn _portIn = new _PortIn(portIn.portFunType());
        _portIn.tiedName = PortUtil.tied(portIn, clazz, isMixin || enableDefaultValue);
        _portIn.inNames = InNames.fromStringArray(portIn.nece(), portIn.unnece(), portIn.inner());
        _portIn.method = portIn.method();
        _portIn.checks = portIn.checks();
        _portIn.checksForWholeClass = portIn.checksForWholeClass();
        _portIn.setTiedType(portIn.tiedType());
        _portIn.ignoreTypeParser = portIn.ignoreTypeParser();

        if (LOGGER.isDebugEnabled() && !isMixin)
        {
            LOGGER.debug("tiedName={},tiedType={},method={}", _portIn.tiedName, _portIn.getTiedType(), _portIn.method);
        }

        return _portIn;
    }

    public _PortIn portIn(Method method, _PortIn class_PortIn)
    {
        _PortIn _portIn = null;
        PortIn portIn = AnnoUtil.getAnnotation(method, PortIn.class);
        if (portIn != null)
        {
//            Class<?>[] parameters = method.getParameterTypes();
//            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0]))
//            {
//                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
//            }
            _portIn = new _PortIn(PortFunType.type(class_PortIn.getPortFunType(), portIn.portFunType()));
            _portIn.setTiedType(TiedType.type(class_PortIn.getTiedType(), portIn.tiedType()));
            _portIn.tiedName = PortUtil
                    .tied(portIn, method, _portIn.getTiedType() == TiedType.REST || enableDefaultValue);
            _portIn.inNames = InNames.fromStringArray(portIn.nece(), portIn.unnece(), portIn.inner());
            _portIn.checks = portIn.checks();
            _portIn.method = AnnoUtil.method(class_PortIn.getMethod(), portIn.method());
            _portIn.ignoreTypeParser = portIn.ignoreTypeParser();

        }
        return _portIn;
    }

    public void setTiedName(_PortIn portIn, String tiedName)
    {
        portIn.setTiedName(tiedName);
    }

    public void setTiedType(_PortIn portIn, TiedType tiedType)
    {
        portIn.setTiedType(tiedType);
    }
}
