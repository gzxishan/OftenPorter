package cn.xishan.oftenporter.porter.core.advanced;


import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.SeekPackages;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import cn.xishan.oftenporter.porter.core.base.InNames.Name;

/**
 * 接口处理的工具类。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PortUtil
{
    private static final String TIED_ACCEPTED = "^[a-zA-Z0-9%_.$&=\\-]+$";
    private static final Pattern TIED_NAME_PATTERN = Pattern.compile(TIED_ACCEPTED);
    private final Logger LOGGER;

    public PortUtil()
    {
        LOGGER = LogUtil.logger(PortUtil.class);
    }

    /**
     * 是否忽略某些类的高级处理。
     *
     * @param clazz
     * @return
     */
    public static final boolean willIgnoreAdvanced(Class clazz)
    {
        Package pkg = clazz.getPackage();
        String pkgName = pkg != null ? pkg.getName() : null;
        if (pkgName != null && (pkgName.startsWith("java.") || pkgName.startsWith("javax.")||clazz.isPrimitive()))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 得到类的绑定名。
     *
     * @param portIn
     * @param clazz
     * @return
     */
    public static String[] tieds(PortIn portIn, Class<?> clazz, SeekPackages.Tiedfix classTiedfix, boolean enableDefaultValue)
    {
        String[] names = tieds(portIn);
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            if (OftenTool.isEmpty(name))
            {
                if (!enableDefaultValue)
                {
                    throw new InitException("default value is not enable for " + clazz);
                }
                name = tiedIgnorePortIn(clazz);
            }
            if(classTiedfix!=null){
                if(classTiedfix.getPrefix()!=null&&(!classTiedfix.isCheckExists()||!name.startsWith(classTiedfix.getPrefix()))){
                    name=classTiedfix.getPrefix()+name;
                }
                if(classTiedfix.getSuffix()!=null&&(!classTiedfix.isCheckExists()||!name.endsWith(classTiedfix.getSuffix()))){
                    name+=classTiedfix.getSuffix();
                }
            }
            names[i] = checkTied(name);
        }
        return names;
    }

    public static Class<?> getRealClass(Object object)
    {
//        if (object instanceof AutoSetObjForAspectOfNormal.IOPProxy)
//        {
//            AutoSetObjForAspectOfNormal.IOPProxy iopProxy =
//                    (AutoSetObjForAspectOfNormal.IOPProxy) object;
//            return iopProxy.get_R_e_a_l_C_l_a_s_s();
//        } else
//        {
//            return object.getClass();
//        }
        return getRealClass(object.getClass());
    }

    public static Class<?> getRealClass(Class mayProxyChildClass)
    {
        if (OftenTool.isAssignable(mayProxyChildClass, AutoSetObjForAspectOfNormal.IOPProxy.class))
        {
            return mayProxyChildClass.getSuperclass();
        } else
        {
            return mayProxyChildClass;
        }
    }

    /**
     * 得到函数的绑定名。
     *
     * @param portIn
     * @param method
     * @return
     */
    public static String[] tieds(PortIn portIn, Method method, boolean enableDefaultValue)
    {
        String[] names = tieds(portIn);
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            if (OftenTool.isEmpty(name))
            {
                if (!enableDefaultValue)
                {
                    throw new InitException("default value is not enable for " + method);
                }
                name = method.getName();
            }
            names[i] = checkTied(name);
        }
        return names;
    }

    private static String tied(PortIn portIn)
    {
        String name = portIn.value();
        if (OftenTool.isEmpty(name))
        {
            name = portIn.tied();
        }
        return name;
    }

    private static String[] tieds(PortIn portIn)
    {
        String[] tieds = portIn.tieds();
        if (tieds.length == 0)
        {
            String name = portIn.value();
            if (OftenTool.isEmpty(name))
            {
                name = portIn.tied();
            }
            tieds = new String[]{name};
        }
        return tieds;
    }

    public static String tied(Unece unece, Field field, boolean enableDefaultValue)
    {
        String name = unece.value();
        String varName = unece.varName();
        return getTied(unece, name, varName, field, enableDefaultValue);
    }

    private static String getTied(Object neceOrUnece, String name, String varName, Field field,
            boolean enableDefaultValue)
    {
        if (OftenTool.isEmptyOfAll(name, varName))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + neceOrUnece + " in field '" + field + "'");
            }
            name = field.getName();
        } else if (OftenTool.isEmpty(name))
        {
            name = varName;
        }
        return name;
    }

    public static String tied(Nece nece, Field field, boolean enableDefaultValue)
    {
        String name = nece.value();
        String varName = nece.varName();
        return getTied(nece, name, varName, field, enableDefaultValue);
    }

    /**
     * 得到类的绑定名。
     *
     * @param portIn
     * @param clazz
     * @return
     */
    public static String tied(PortIn portIn, Class<?> clazz, boolean enableDefaultValue)
    {
        String name = tied(portIn);
        if (OftenTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + clazz);
            }
            return tiedIgnorePortIn(clazz);
        } else
        {
            return checkTied(name);
        }
    }

    public static String tied(Class<?> clazz)
    {
        PortIn portIn = AnnoUtil.getAnnotation(clazz, PortIn.class);
        String tiedName = portIn != null ? tied(portIn) : null;
        if (OftenTool.isEmpty(tiedName))
        {
            tiedName = tiedIgnorePortIn(clazz);
        }
        return tiedName;
    }

    public static String tiedIgnorePortIn(Class<?> clazz)
    {
        String className = clazz.getSimpleName();
        if (className.endsWith("WPort"))
        {
            className = className.substring(0, className.length() - 4);
        } else if (className.endsWith("Porter"))
        {
            className = className.substring(0, className.length() - 6);
        }
        if (className.equals(""))
        {
            className = clazz.getSimpleName();
        }
        return checkTied(className);
    }


    /**
     * 判断是否是接口类。
     *
     * @param clazz 要判断的类。
     * @return
     */
    public static boolean isJustPortInClass(Class<?> clazz)
    {
        return !Modifier.isAbstract(clazz.getModifiers()) && AnnoUtil
                .isOneOfAnnotationsPresent(clazz, PortIn.class) && !AnnoUtil
                .isOneOfAnnotationsPresent(clazz, MixinOnly.class) && getMixinTos(clazz).length == 0;
    }

    /**
     * 是否是Porter或混入Porter。
     * @param clazz
     * @return
     */
    public static boolean isPorter(Class clazz){
        return isJustPortInClass(clazz)||isMixinPortClass(clazz);
    }

    /**
     * 判断是否是接口类。
     *
     * @param clazz 要判断的类。
     * @return
     */
    public static boolean isMixinPortClass(Class<?> clazz)
    {

        return !Modifier.isAbstract(clazz.getModifiers()) && (AnnoUtil
                .isOneOfAnnotationsPresent(clazz, PortIn.class, MixinOnly.class) || getMixinTos(clazz).length > 0);
    }

    public static MixinTo[] getMixinTos(Class<?> clazz)
    {
        MixinTo[] mixinTos = AnnoUtil.getRepeatableAnnotations(clazz, MixinTo.class);
        return mixinTos;
    }


    public static boolean enableMixinByMixinTo(Class<?> clazz)
    {
        MixinTo[] mixinTos = getMixinTos(clazz);
        boolean enable = true;
        for (MixinTo mixinTo : mixinTos)
        {
            if (!mixinTo.enableMixin())
            {
                enable = false;
                break;
            }
        }
        return enable;
    }

    /**
     * 检查名称是否含有非法字符。
     *
     * @param name
     * @return
     */
    public static void checkName(String name) throws RuntimeException
    {
        checkTied(name);
    }

    private static String checkTied(String tiedName)
    {
        if (!TIED_NAME_PATTERN.matcher(tiedName).find())
        {
            throw new RuntimeException("Illegal value '" + tiedName + "'(accepted-pattern:" + TIED_ACCEPTED + ")");
        }
        return tiedName;
    }



    public static Object[] newArray(Name[] names)
    {
        if (names.length == 0)
        {
            return OftenTool.EMPTY_OBJECT_ARRAY;
        } else
        {
            return new Object[names.length];
        }
    }


    /**
     * 返回结果不为null。
     * 返回{@linkplain ParamDealt.FailedReason}表示失败，否则成功。
     */
    public Object paramDealOne(OftenObject oftenObject, boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            String optionKey,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        return paramDealOne(oftenObject, ignoreTypeParser, paramDealt, one, optionKey, paramSource, currentTypeParserStore,
                "");
    }

    /**
     * 返回结果不为null。
     * 返回{@linkplain ParamDealt.FailedReason}表示失败，否则成功。
     */
    private Object paramDealOne(OftenObject oftenObject, boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            String optionKey,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore, String namePrefix)
    {
        Object obj;
        try
        {

            {
                Object value = null;
                if (OftenTool.notNullAndEmpty(optionKey))
                {
                    value = paramSource.getParam(optionKey);
                }
                if (value == null)
                {
                    value = paramSource.getParam(one.clazz.getName());
                }
                if (value != null && OftenTool.isAssignable(value.getClass(), one.clazz))
                {
                    Name[] neces = one.inNames.nece;
                    //判断必须值
                    for (int i = 0; i < neces.length; i++)
                    {
                        Field f = one.neceObjFields[i];
                        if (neces[i].getNece().isNece(oftenObject) && OftenTool.isEmpty(f.get(value)))
                        {
                            value = DefaultFailedReason
                                    .lackNecessaryParams("Lack necessary params!", neces[i].varName);
                            break;
                        }
                    }
                    if (!(value instanceof ParamDealt.FailedReason))
                    {
                        //转换内嵌对象
                        Object fieldObject = parseInnerOnes(oftenObject, ignoreTypeParser, paramDealt, one, paramSource,
                                value, currentTypeParserStore, namePrefix);//见DefaultParamDealt.getParam
                        if (fieldObject instanceof ParamDealt.FailedReason)
                        {
                            value = fieldObject;
                        }
                    }

                    return value;//如果获取的变量是相应类型，直接返回。
                }
            }
            Object[] neces = PortUtil.newArray(one.inNames.nece);
            Object[] unneces = PortUtil.newArray(one.inNames.unece);
            Object reason = paramDeal(oftenObject, ignoreTypeParser, paramDealt, one.inNames, neces, unneces, paramSource,
                    currentTypeParserStore, namePrefix);
            if (reason == null)
            {
                Object object = OftenTool.newObject(one.clazz);

                for (int k = 0; k < neces.length; k++)
                {
                    one.neceObjFields[k].set(object, neces[k]);
                }

                for (int k = 0; k < unneces.length; k++)
                {
                    if (unneces[k] != null)
                    {
                        one.unneceObjFields[k].set(object, unneces[k]);
                    }
                }
                obj = object;

                //转换内嵌对象
                Object fieldObject = parseInnerOnes(oftenObject, ignoreTypeParser, paramDealt, one, paramSource, object,
                        currentTypeParserStore, namePrefix);
                if (fieldObject instanceof ParamDealt.FailedReason)
                {
                    obj = fieldObject;
                }
            } else
            {
                obj = reason;
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            obj = DefaultFailedReason.parseOftenEntitiesException(OftenTool.getMessage(e));
        }
        return obj;
    }

    private Object parseInnerOnes(OftenObject oftenObject, boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            ParamSource paramSource, Object object,
            TypeParserStore currentTypeParserStore, String namePrefix) throws Exception
    {
        Object obj = null;
        //转换内嵌对象
        for (int i = 0; i < one.jsonObjFields.length; i++)
        {

            Object fieldObject = paramDealOne(oftenObject, ignoreTypeParser, paramDealt, one.jsonObjOnes[i], null,
                    paramSource, currentTypeParserStore, namePrefix + one.jsonObjVarnames[i] + ".");
            if (fieldObject instanceof ParamDealt.FailedReason)
            {
                obj = fieldObject;
                break;
            } else
            {
                one.jsonObjFields[i].set(object, fieldObject);
            }
        }
        return obj;
    }

    /**
     * 参数处理
     *
     * @return 返回null表示转换成功，否则表示失败。
     */
    public ParamDealt.FailedReason paramDeal(OftenObject oftenObject, boolean ignoreTypeParser, ParamDealt paramDealt,
            InNames inNames,
            Object[] nece,
            Object[] unece,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        return paramDeal(oftenObject, ignoreTypeParser, paramDealt, inNames, nece, unece, paramSource,
                currentTypeParserStore, "");
    }


    /**
     * 参数处理
     *
     * @return 返回null表示转换成功，否则表示失败。
     */
    private ParamDealt.FailedReason paramDeal(OftenObject oftenObject, boolean ignoreTypeParser, ParamDealt paramDealt,
            InNames inNames,
            Object[] nece,
            Object[] unece,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore, String namePrefix)
    {
        ParamDealt.FailedReason reason = null;
        try
        {
            if (ignoreTypeParser)
            {
                Name[] names = inNames.nece;
                for (int i = 0; i < nece.length; i++)
                {
                    _Nece neceDeal = names[i].getNece();
                    if (neceDeal == null || neceDeal.isNece(oftenObject))
                    {
                        nece[i] = paramSource.getNeceParam(namePrefix + names[i].varName);
                    } else
                    {
                        nece[i] = paramSource.getParam(namePrefix + names[i].varName);
                    }
                }
                names = inNames.unece;
                for (int i = 0; i < unece.length; i++)
                {
                    unece[i] = paramSource.getParam(namePrefix + names[i].varName);
                }
            } else
            {
                reason = paramDealt.deal(oftenObject, inNames.nece, nece, true, paramSource,
                        currentTypeParserStore, namePrefix);
                if (reason == null)
                {
                    reason = paramDealt.deal(oftenObject, inNames.unece, unece, false,
                            paramSource, currentTypeParserStore, namePrefix);
                }
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            reason = DefaultFailedReason.parseOftenEntitiesException(OftenTool.getMessage(e));
        }
        return reason;
    }


}
