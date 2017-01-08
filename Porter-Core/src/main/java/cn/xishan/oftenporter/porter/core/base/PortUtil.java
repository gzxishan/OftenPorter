package cn.xishan.oftenporter.porter.core.base;


import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String TIED_ACCEPTED = "^[a-zA-Z0-9%_.$&=-]+$";
    private static final Pattern TIED_NAME_PATTERN = Pattern.compile(TIED_ACCEPTED);
    private static final Logger LOGGER = LoggerFactory.getLogger(PortUtil.class);

    /**
     * 得到函数的绑定名。
     *
     * @param portIn
     * @param method
     * @return
     */
    public static String tied(PortIn portIn, Method method, boolean enableDefaultValue)
    {
        String name = portIn.value();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + method);
            }
            name = method.getName();
        }
        return checkTied(name);
    }

    public static String tied(PortInObj.UnNece unNece, Field field, boolean enableDefaultValue)
    {
        String name = unNece.value();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + unNece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
    }

    public static String tied(PortInObj.Nece nece, Field field, boolean enableDefaultValue)
    {
        String name = nece.value();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + nece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
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
        String name = portIn.value();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + clazz);
            }
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
            name = className;
        }
        return checkTied(name);
    }

    /**
     * 判断是否是接口类。
     *
     * @param clazz 要判断的类。
     * @return
     */
    public static boolean isPortClass(Class<?> clazz)
    {
        return !Modifier.isAbstract(clazz.getModifiers()) && clazz.isAnnotationPresent(PortIn.class);
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
    public static Object paramDealOne(boolean ignoreTypeParser, ParamDealt paramDealt, One one,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        Object obj;
        try
        {
            Object[] neces = PortUtil.newArray(one.inNames.nece);
            Object[] unneces = PortUtil.newArray(one.inNames.unece);
            Object reason = paramDeal(ignoreTypeParser, paramDealt, one.inNames, neces, unneces, paramSource,
                    currentTypeParserStore);
            if (reason == null)
            {
                Object object = WPTool.newObject(one.clazz);

                for (int k = 0; k < neces.length; k++)
                {
                    one.neceObjFields[k].set(object, neces[k]);
                }

                for (int k = 0; k < unneces.length; k++)
                {
                    if (!(unneces[k] == null && one.unneceObjFields[k].getType().isPrimitive()))
                    {
                        one.unneceObjFields[k].set(object, unneces[k]);
                    }
                }

                obj = object;
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
    public static ParamDealt.FailedReason paramDeal(boolean ignoreTypeParser, ParamDealt paramDealt, InNames inNames,
            Object[] nece,
            Object[] unnece,
            ParamSource paramSource,
            TypeParserStore currentTypeParserStore)
    {
        ParamDealt.FailedReason reason=null;
        try
        {
            if (ignoreTypeParser)
            {
                Name[] names = inNames.nece;
                for (int i = 0; i < nece.length; i++)
                {
                    nece[i] = paramSource.getParam(names[i].varName);
                }
                names = inNames.unece;
                for (int i = 0; i < unnece.length; i++)
                {
                    unnece[i] = paramSource.getParam(names[i].varName);
                }
            } else
            {
                reason = paramDealt.deal(inNames.nece, nece, true, paramSource, currentTypeParserStore);
                if (reason == null)
                {
                    reason = paramDealt.deal(inNames.unece, unnece, false, paramSource, currentTypeParserStore);
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
