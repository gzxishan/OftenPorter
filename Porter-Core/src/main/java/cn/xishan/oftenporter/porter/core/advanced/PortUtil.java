package cn.xishan.oftenporter.porter.core.advanced;


import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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
     * 得到类的绑定名。
     *
     * @param portIn
     * @param clazz
     * @return
     */
    public static String[] tieds(PortIn portIn, Class<?> clazz, boolean enableDefaultValue)
    {

        String[] names = tieds(portIn);
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            if (WPTool.isEmpty(name))
            {
                if (!enableDefaultValue)
                {
                    throw new InitException("default value is not enable for " + clazz);
                }
                name = tiedIgnorePortIn(clazz);
            }
            names[i] = checkTied(name);
        }
        return names;
    }

    public static Class<?> getRealClass(Object object)
    {
        if (object instanceof AutoSetObjForAspectOfNormal.IOPProxy)
        {
            AutoSetObjForAspectOfNormal.IOPProxy iopProxy =
                    (AutoSetObjForAspectOfNormal.IOPProxy) object;
            return iopProxy.get_R_e_a_l_C_l_a_s_s();
        } else
        {
            return object.getClass();
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
            if (WPTool.isEmpty(name))
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
        if (WPTool.isEmpty(name))
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
            if (WPTool.isEmpty(name))
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
        if (WPTool.isEmptyOfAll(name, varName))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + neceOrUnece + " in field '" + field + "'");
            }
            name = field.getName();
        } else if (WPTool.isEmpty(name))
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
        if (WPTool.isEmpty(name))
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
        PortIn portIn = AnnoUtil.Advanced.getAnnotation(clazz, PortIn.class);
        String tiedName = portIn != null ? tied(portIn) : null;
        if (WPTool.isEmpty(tiedName))
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
        return !Modifier.isAbstract(clazz.getModifiers()) && clazz.isAnnotationPresent(PortIn.class) && !AnnoUtil
                .isOneOfAnnotationsPresent(clazz, MixinOnly.class, MixinTo.class);
    }


    public static Class getMixinToContextSetKey(Class clazz)
    {
        MixinTo mixinTo = AnnoUtil.Advanced.getAnnotation(clazz, MixinTo.class);
        if (mixinTo != null && !AutoSet.class.equals(mixinTo.toContextSetWithClassKey()))
        {
            return mixinTo.toContextSetWithClassKey();
        } else
        {
            return null;
        }
    }

    /**
     * 判断是否是接口类。
     *
     * @param clazz 要判断的类。
     * @return
     */
    public static boolean isMixinPortClass(Class<?> clazz)
    {

        return !Modifier.isAbstract(clazz.getModifiers()) && AnnoUtil
                .isOneOfAnnotationsPresent(clazz, PortIn.class, MixinOnly.class, MixinTo.class);
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


    private static final Object[] EMPTY = new Object[0];

    public static Object[] newArray(Name[] names)
    {
        if (names.length == 0)
        {
            return EMPTY;
        } else
        {
            return new Object[names.length];
        }
    }


    /**
     * 返回结果不为null。
     * 返回{@linkplain ParamDealt.FailedReason}表示失败，否则成功。
     */
    public Object paramDealOne(WObject wObject, boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        return paramDealOne(wObject, ignoreTypeParser, paramDealt, one, paramSource, currentTypeParserStore, "");
    }

    /**
     * 返回结果不为null。
     * 返回{@linkplain ParamDealt.FailedReason}表示失败，否则成功。
     */
    private Object paramDealOne(WObject wObject, boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore, String namePrefix)
    {
        Object obj;
        try
        {
            Object[] neces = PortUtil.newArray(one.inNames.nece);
            Object[] unneces = PortUtil.newArray(one.inNames.unece);
            Object reason = paramDeal(wObject, ignoreTypeParser, paramDealt, one.inNames, neces, unneces, paramSource,
                    currentTypeParserStore, namePrefix);
            if (reason == null)
            {
                Object object = WPTool.newObject(one.clazz);

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
                for (int i = 0; i < one.jsonObjFields.length; i++)
                {

                    Object fieldObject = paramDealOne(wObject, ignoreTypeParser, paramDealt, one.jsonObjOnes[i],
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
            } else
            {
                obj = reason;
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            obj = DefaultFailedReason.parsePortInObjException(WPTool.getMessage(e));
        }
        return obj;
    }

    /**
     * 参数处理
     *
     * @return 返回null表示转换成功，否则表示失败。
     */
    public ParamDealt.FailedReason paramDeal(WObject wObject, boolean ignoreTypeParser, ParamDealt paramDealt,
            InNames inNames,
            Object[] nece,
            Object[] unece,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        return paramDeal(wObject, ignoreTypeParser, paramDealt, inNames, nece, unece, paramSource,
                currentTypeParserStore, "");
    }


    /**
     * 参数处理
     *
     * @return 返回null表示转换成功，否则表示失败。
     */
    private ParamDealt.FailedReason paramDeal(WObject wObject, boolean ignoreTypeParser, ParamDealt paramDealt,
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
                    if (inNames.neceDeals == null || inNames.neceDeals[i].isNece(wObject))
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
                reason = paramDealt.deal(wObject, inNames.nece, inNames.neceDeals, nece, true, paramSource,
                        currentTypeParserStore, namePrefix);
                if (reason == null)
                {
                    reason = paramDealt
                            .deal(wObject, inNames.unece, null, unece, false, paramSource, currentTypeParserStore,
                                    namePrefix);
                }
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            reason = DefaultFailedReason.parsePortInObjException(WPTool.getMessage(e));
        }
        return reason;
    }


}
