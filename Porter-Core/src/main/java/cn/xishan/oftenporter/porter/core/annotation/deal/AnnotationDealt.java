package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IFun;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.param.*;
import cn.xishan.oftenporter.porter.core.annotation.sth.ObjectGetter;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterParamGetterImpl;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
@PortIn
@MixinOnly
public final class AnnotationDealt
{
    private boolean enableDefaultValue;
    private final Logger LOGGER;
    private Map<Class, Method[]> destroyMethodsMap;
    private Map<Class, Method[]> startMethodsMap;
    private Map<String, _PortDestroy> destroyMap;
    private Map<String, _PortStart> startMap;

    private static final _PortDestroy PORT_DESTROY_EMPTY = new _PortDestroy();
    private static final _PortStart PORT_START_EMPTY = new _PortStart();

    private AnnotationDealt(boolean enableDefaultValue)
    {
        this.enableDefaultValue = enableDefaultValue;
        LOGGER = LogUtil.logger(AnnotationDealt.class);
        clearCache();
    }

    /**
     * @param enableDefaultValue 是否允许{@linkplain PortIn#value()}取默认值。
     */
    public static AnnotationDealt newInstance(boolean enableDefaultValue)
    {
        return new AnnotationDealt(enableDefaultValue);
    }

    public void clearCache()
    {
        destroyMethodsMap = new HashMap<>();
        startMethodsMap = new HashMap<>();
        destroyMap = new HashMap<>();
        startMap = new HashMap<>();
    }

    public _SyncPorterOption syncPorterOption(Field field,PorterParamGetterImpl porterParamGetter)

    {
        SyncPorterOption option = AnnoUtil.getAnnotation(field, SyncPorterOption.class);
        if (option != null)
        {
            _SyncPorterOption syncPorterOption = new _SyncPorterOption(porterParamGetter);
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
        AutoSet autoSet = AnnoUtil.getAnnotation(field, AutoSet.class);
        if (autoSet == null)
        {
            try
            {
                Class.forName("javax.annotation.Resource");
                Resource resource = AnnoUtil.getAnnotation(field, Resource.class);
                if (resource != null)
                {
                    LOGGER.debug("new autoset from @Resource={},field={}", resource, field);
                    _AutoSet _autoSet = new _AutoSet();
                    _autoSet.willRecursive = false;
                    _autoSet.value = resource.name();
                    _autoSet.nullAble = true;
                    if (!resource.type().equals(Object.class))
                    {
                        _autoSet.classValue = resource.type();
                    }
                    if (!resource.shareable())
                    {
                        _autoSet.range = AutoSet.Range.New;
                    } else if (resource.authenticationType() == Resource.AuthenticationType.CONTAINER)
                    {
                        _autoSet.range = AutoSet.Range.Global;
                    } else if (resource.authenticationType() == Resource.AuthenticationType.APPLICATION)
                    {
                        _autoSet.range = AutoSet.Range.Context;
                    }
                    return _autoSet;
                }
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
        _AutoSet _autoSet = new _AutoSet();
        _autoSet.classValue = autoSet.classValue();
        _autoSet.value = autoSet.value();
        _autoSet.range = autoSet.range();
        _autoSet.nullAble = autoSet.nullAble();
        _autoSet.notNullPut = autoSet.notNullPut();
        _autoSet.dealt = autoSet.dealt();
        _autoSet.gen = autoSet.gen();
        _autoSet.option = autoSet.option();
        return _autoSet;
    }

    public _Nece nece(Field field)
    {
        Nece nece = AnnoUtil.getAnnotation(field, Nece.class);
        if (nece == null)
        {
            return null;
        }
        return nece(nece, field.getName());
    }

    public _Nece nece(Nece nece, String fieldName)
    {
        if (nece == null)
        {
            return null;
        }
        _Nece _nece = new _Nece();
        if (WPTool.isEmptyOfAll(nece.value(), nece.varName()))
        {
            _nece.value = fieldName;
        } else if (WPTool.notNullAndEmpty(nece.value()))
        {
            _nece.value = nece.value();
        } else
        {
            _nece.value = nece.varName();
        }
        _nece.toUnece = nece.toUnece();
        _nece.forMethods = nece.forMethods();
        _nece.forClassTieds = nece.forClassTieds();
        _nece.forFunTieds = nece.forFunTieds();

        _nece.init();
        return _nece;
    }

    public _Unece unNece(Field field)
    {
        Unece unece = AnnoUtil.getAnnotation(field, Unece.class);
        if (unece == null)
        {
            return null;
        }
        return unNece(unece, field.getName());
    }

    public _Unece unNece(Unece unece, String fieldName)
    {
        if (unece == null)
        {
            return null;
        }
        _Unece _unece = new _Unece();

        if (WPTool.isEmptyOfAll(unece.value(), unece.varName()))
        {
            _unece.value = fieldName;
        } else if (WPTool.notNullAndEmpty(unece.value()))
        {
            _unece.value = unece.value();
        } else
        {
            _unece.value = unece.varName();
        }
        return _unece;
    }

    public _BindEntities bindEntities(Class<?> porterClass, Method method)
    {
        List<Class> classList = new ArrayList<>();

        OnPorterEntities onPorterEntities = AnnoUtil.getAnnotation(porterClass, OnPorterEntities.class);

        BindEntities bindEntities = AnnoUtil.getAnnotation(method, BindEntities.class);
        FromPorterEntities fromPorterEntities = AnnoUtil.getAnnotation(method, FromPorterEntities.class);

        if (bindEntities != null)
        {
            WPTool.addAll(classList, bindEntities.value());
        }

        if (fromPorterEntities != null)
        {
            if (onPorterEntities == null)
            {

                if (fromPorterEntities.value().length > 0)
                {
                    LOGGER.warn("need @{}.{} in [{}] for method with @", BindEntities.class.getSimpleName(),
                            OnPorterEntities.class.getSimpleName(),
                            porterClass.getName(), FromPorterEntities.class.getSimpleName());
                }
            } else
            {
                Class<?>[] fclasses = fromPorterEntities.value();
                Class<?>[] pclasses = onPorterEntities.value();
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
                        throw new InitException(
                                "not found [" + fclasses[i].getName() + "] in @" + OnPorterEntities.class
                                        .getSimpleName() + " in [" + porterClass
                                        .getName() + "] for method with @" + FromPorterEntities.class.getSimpleName());
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
        _BindEntities _bindEntities = newBindEntities(classList.toArray(new Class[0]), method);
        return _bindEntities;

    }


    private _BindEntities newBindEntities(Class<?>[] _classes, Method method)
    {
        _BindEntities.CLASS[] classes = new _BindEntities.CLASS[_classes.length];
        for (int i = 0; i < classes.length; i++)
        {
            Class<?> clazz = _classes[i];
            BindEntityDealt bindEntityDealt = AnnoUtil.getAnnotation(clazz, BindEntityDealt.class);
            if (bindEntityDealt != null)
            {
                Class<? extends BindEntityDealt.IHandle> handleClass = bindEntityDealt.handle();
                try
                {
                    classes[i] = new _BindEntities.CLASS(clazz, method, bindEntityDealt.option(),
                            WPTool.newObject(handleClass));
                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            } else
            {
                classes[i] = new _BindEntities.CLASS(clazz);
            }
        }

        _BindEntities _bindEntities = new _BindEntities();
        _bindEntities.value = classes;
        return _bindEntities;
    }

    public _BindEntities bindEntity(Class<?> entityClass, Method method)
    {
        _BindEntities _bindEntities = newBindEntities(new Class[]{entityClass}, method);
        return _bindEntities;
    }

    public _BindEntities bindEntities(Class<?> clazz)
    {
        BindEntities bindEntities = AnnoUtil.getAnnotation(clazz, BindEntities.class);
        if (bindEntities == null)
        {
            return null;
        }
        _BindEntities _bindEntities = newBindEntities(bindEntities.value(), null);
        return _bindEntities;
    }

    public _Parse[] parses(Method method)
    {
        return to_parses(AnnoUtil.getRepeatableAnnotations(method, Parse.class));
    }

    public _Parse[] parses(Class<?> clazz)
    {
        return to_parses(AnnoUtil.getRepeatableAnnotations(clazz, Parse.class));
    }


    public _Parse[] parses(Field field)
    {
        Parse parse = AnnoUtil.getAnnotation(field, Parse.class);
        if (parse == null)
        {
            return EMPTY_PARSES;
        }
        return to_parses(new Parse[]{parse});
    }

    private static final _Parse[] EMPTY_PARSES = new _Parse[0];

    private _Parse[] to_parses(Parse[] parses)
    {
        if (parses.length == 0)
        {
            return EMPTY_PARSES;
        }
        _Parse[] _ps = new _Parse[parses.length];
        for (int i = 0; i < _ps.length; i++)
        {
            _ps[i] = genParse(parses[i]);
        }

        return _ps;
    }

    public _Parse genParse(Parse parse)
    {
        _Parse _p = new _Parse();
        _p.paramNames = parse.paramNames();
        _p.parserName = parse.parserName();
        _p.parserClass = parse.parser();
        return _p;
    }


    private boolean isNullInstanceOfStartDestroy(Method method, @MayNull ObjectGetter objectGetter)
    {
        if (!Modifier.isStatic(method.getModifiers()) && (objectGetter == null || objectGetter.getObject() == null))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public _PortDestroy portDestroy(Method method, @MayNull ObjectGetter objectGetter)
    {
        String mkey = method.toString();
        _PortDestroy _portDestroy = destroyMap.get(mkey);
        if (_portDestroy == PORT_DESTROY_EMPTY)
        {
            return null;
        } else if (_portDestroy == null)
        {
            if (isNullInstanceOfStartDestroy(method, objectGetter))
            {
//            if (LOGGER.isWarnEnabled())
//            {
//                LOGGER.warn("ignore {} for no instance:{}", PortDestroy.class.getSimpleName(), method);
//            }
                destroyMap.put(mkey, PORT_DESTROY_EMPTY);
                return null;
            }

            PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
            if (portDestroy == null)
            {
                destroyMap.put(mkey, PORT_DESTROY_EMPTY);
                return null;
            }
            _portDestroy = new _PortDestroy();
            _portDestroy.porterOfFun = PorterOfFun.withMethodAndObject(method, objectGetter);
            _portDestroy.order = portDestroy.order();
            destroyMap.put(mkey, _portDestroy);
        }

        return _portDestroy;
    }


    public _PortStart portStart(Method method, @MayNull ObjectGetter objectGetter)
    {
        String mkey = method.toString();
        _PortStart _portStart = startMap.get(mkey);
        if (_portStart == PORT_START_EMPTY)
        {
            return null;
        } else if (_portStart == null)
        {
            if (isNullInstanceOfStartDestroy(method, objectGetter))
            {
//            if (LOGGER.isWarnEnabled())
//            {
//                LOGGER.warn("ignore {} for no instance:{}", PortStart.class.getSimpleName(), method);
//            }
                startMap.put(mkey, PORT_START_EMPTY);
                return null;
            }

            PortStart portStart = AnnoUtil.getAnnotation(method, PortStart.class);
            if (portStart == null)
            {
                startMap.put(mkey, PORT_START_EMPTY);
                return null;
            }
            _portStart = new _PortStart();
            _portStart.porterOfFun = PorterOfFun.withMethodAndObject(method, objectGetter);
            _portStart.order = portStart.order();
            startMap.put(mkey, _portStart);
        }

        return _portStart;
    }

    public Method[] getPortStart(Object object, Class objectClass)
    {
        objectClass = PortUtil.getRealClass(objectClass);
        Method[] methods = startMethodsMap.get(objectClass);
        if (methods == null)
        {
            ObjectGetter objectGetter = () -> object;

            methods = WPTool.getAllPublicMethods(objectClass);
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
            methods = new Method[portStarts.length];
            for (int i = 0; i < portStarts.length; i++)
            {
                methods[i] = portStarts[i].porterOfFun.getMethod();
            }
            startMethodsMap.put(objectClass, methods);
        }

        return methods;
    }

    public Method[] getPortDestroy(Object object, Class objectClass)
    {
        objectClass = PortUtil.getRealClass(objectClass);
        Method[] methods = destroyMethodsMap.get(objectClass);

        if (methods == null)
        {
            ObjectGetter objectGetter = () -> object;

            methods = WPTool.getAllMethods(objectClass);
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
            methods = new Method[portDestroys.length];
            for (int i = 0; i < portDestroys.length; i++)
            {
                methods[i] = portDestroys[i].porterOfFun.getMethod();
            }
            destroyMethodsMap.put(objectClass, methods);
        }

        return methods;
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
        PortIn portIn = AnnoUtil.getAnnotation(clazz, PortIn.class);
        if (portIn == null && isMixin)
        {
            portIn = AnnoUtil.getAnnotation(AnnotationDealt.class, PortIn.class);
        } else if (portIn == null || (!isMixin && (AnnoUtil
                .isOneOfAnnotationsPresent(clazz, MixinOnly.class) || PortUtil.getMixinTos(clazz).length > 0)))
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
        _portIn.toPorterKey = portIn.toPorterKey();
        if (_portIn.toPorterKey.equals(PortIn.class))
        {
            _portIn.toPorterKey = clazz;
        }
        return _portIn;
    }

    public _PortIn portIn(Porter porter, Method method)
    {
        _PortIn class_PortIn = porter.getPortIn();
        _PortIn _portInOfMethod = null;
        PortIn portIn = AnnoUtil.getAnnotation(method, PortIn.class);
        if (portIn != null)
        {

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
            if (porter.getObj() instanceof IFun)
            {
                IFun iFun = (IFun) porter.getObj();
                if (!_portInOfMethod.getTiedType().isRest())
                {
                    String[] tieds = iFun.tieds(porter, method, _portInOfMethod);
                    for (String tied : tieds)
                    {
                        PortUtil.checkName(tied);
                    }
                    _portInOfMethod.tiedNames = tieds;
                }
            }
        }
        return _portInOfMethod;
    }

    public void setClassTiedName(_PortIn portIn, String tiedName)
    {
        portIn.setTiedNames(new String[]{tiedName});
    }

    public void setTiedType(_PortIn portIn, TiedType tiedType)
    {
        if (tiedType == null)
        {
            return;
        }
        portIn.setTiedType(tiedType);
    }

    public void setMethods(_PortIn portIn, PortMethod[] portMethods)
    {
        portIn.methods = portMethods;
    }
}
