package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal._Parser;
import cn.xishan.oftenporter.porter.core.annotation.deal._parse;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
class SthUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SthUtil.class);


    /**
     * 对{@linkplain Parser}和{@linkplain Parser.parse}的处理
     *
     * @return 有其中一个注解，返回true；否则返回false。
     */
    static boolean bindParserAndParse(Class<?> clazz, InnerContextBridge innerContextBridge, InNames inNames,
            BackableSeek backableSeek)
    {
        return bindParserAndParse(innerContextBridge.annotationDealt.parser(clazz),
                innerContextBridge.annotationDealt.parse(clazz), inNames,
                innerContextBridge.innerBridge.globalParserStore,
                backableSeek, BackableSeek.SeekType.Add_NotBind);
    }

    /**
     * 对{@linkplain Parser}和{@linkplain Parser.parse}的处理
     *
     * @return 有其中一个注解，返回true；否则返回false。
     */
    static boolean bindParserAndParse(Method method, AnnotationDealt annotationDealt, InNames inNames,
            TypeParserStore typeParserStore, BackableSeek backableSeek)
    {
        return bindParserAndParse(annotationDealt.parser(method), annotationDealt.parse(method), inNames,
                typeParserStore, backableSeek, BackableSeek.SeekType.Add_Bind);
    }

    /**
     * 对{@linkplain Parser}和{@linkplain Parser.parse}的处理
     *
     * @return 有其中一个注解，返回true；否则返回false。
     */
    private static boolean bindParserAndParse(_Parser parser, _parse parse, InNames inNames,
            TypeParserStore typeParserStore, BackableSeek backableSeek, BackableSeek.SeekType seekType)
    {
        if (parser != null)
        {
            SthUtil.bindTypeParsers(inNames, parser, typeParserStore, backableSeek, seekType);
        }

        if (parse != null)
        {
            SthUtil.bindTypeParser(inNames, parse, typeParserStore, backableSeek, seekType);
        }
        return parser != null || parse != null;
    }

    /**
     * 查找多个{@linkplain Parser.parse}绑定
     *
     * @param inNames         输入参数
     * @param parser
     * @param typeParserStore 转换器Store
     * @param backableSeek
     * @param seekType
     */
    private static void bindTypeParsers(InNames inNames, _Parser parser,
            TypeParserStore typeParserStore, BackableSeek backableSeek, BackableSeek.SeekType seekType)
    {
        _parse[] parses = parser.get_parses();
        if (parses.length == 0)
        {
            return;
        }

        //注意！！！：对于前n-1一个，不能执行Bind操作。
        BackableSeek.SeekType type = seekType == BackableSeek.SeekType.Add_Bind ? BackableSeek.SeekType.Add_NotBind :
                BackableSeek.SeekType.NotAdd_NotBind;
        for (int i = 0; i < parses.length - 1; i++)
        {
            bindTypeParser(inNames, parses[i], typeParserStore, backableSeek, type);
        }
        bindTypeParser(inNames, parses[parses.length - 1], typeParserStore, backableSeek, seekType);
    }

    static void bindTypeParser(InNames inNames, _parse parse,
            TypeParserStore typeParserStore, BackableSeek backableSeek, BackableSeek.SeekType seekType)
    {
        if (parse != null)
        {
            Class<? extends ITypeParser> parser = parse.getParserClass();
            String varName = parse.getVarName();
            String parserName = parse.getParserName();

            if (!parser.equals(ITypeParser.class))
            {
                ITypeParser iTypeParser = typeParserStore.byId(parser.getName());
                String typeId;
                if (iTypeParser == null)
                {
                    typeId = putTypeParser(parser, typeParserStore);
                } else
                {
                    typeId = iTypeParser.id();
                }
                if (typeId != null)
                {
                    if (inNames != null)
                    {
                        BackableSeek.bindVarNameWithTypeId(inNames, varName, typeId);
                    }
                    if (seekType == BackableSeek.SeekType.Add_Bind || seekType == BackableSeek.SeekType
                            .Add_NotBind)
                    {
                        backableSeek.put(varName, typeId);
                    }
                }

            } else if (!"".equals(parserName) && inNames != null)
            {
                ITypeParser iTypeParser = typeParserStore.byId(parserName);
                if (iTypeParser != null)
                {
                    BackableSeek.bindVarNameWithTypeId(inNames, varName, iTypeParser.id());
                }
            }

        }

        if ((seekType == BackableSeek.SeekType.Add_Bind || seekType == BackableSeek.SeekType.NotAdd_Bind) && inNames
                != null)
        {
            backableSeek.bindTypeId2NameNull(inNames);
        }

    }

    static String putTypeParser(Class<? extends ITypeParser> clazz,
            TypeParserStore typeParserStore)
    {
        ITypeParser typeParser;
        try
        {
            typeParser = WPTool.newObject(clazz);
        } catch (Exception e)
        {
            return null;
        }
        String id = typeParser.id();
        if (!typeParserStore.contains(typeParser))
        {
            typeParserStore.putParser(typeParser);
        }
        return id;
    }

    static void addCheckPassable(Map<Class<?>, CheckPassable> checkPassableMap,
            Class<? extends CheckPassable>[] checks)
    {
        for (int i = 0; i < checks.length; i++)
        {
            if (!checkPassableMap.containsKey(checks[i]))
            {
                try
                {
                    checkPassableMap.put(checks[i], WPTool.newObject(checks[i]));
                } catch (Exception e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
