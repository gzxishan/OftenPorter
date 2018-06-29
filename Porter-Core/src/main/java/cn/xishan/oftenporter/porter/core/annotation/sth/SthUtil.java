package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
class SthUtil
{
    private static final Logger LOGGER = LogUtil.logger(SthUtil.class);

    public SthUtil()
    {
    }

    /**
     * 对MixinParser指定的类的{@linkplain Parser}和{@linkplain Parser.parse}的处理
     */
    void bindParserAndParseWithMixin(Class<?> clazz, InnerContextBridge innerContextBridge, InNames inNames,
            BackableSeek backableSeek, boolean needCheckLoop,
            Map<Class, Set<_MixinPorter>> mixinToMap) throws FatalInitException
    {
        if (needCheckLoop)
        {
            checkLoopMixin(clazz, false, mixinToMap);
        }
        Class<?>[] cs = getMixinParser(clazz);
        for (Class<?> c : cs)
        {
            bindParserAndParse(innerContextBridge.annotationDealt.parser(c),
                    innerContextBridge.annotationDealt.parse(c), inNames,
                    innerContextBridge.innerBridge.globalParserStore,
                    backableSeek, BackableSeek.SeekType.Add_NotBind);
            //递归
            bindParserAndParseWithMixin(c, innerContextBridge, inNames, backableSeek, needCheckLoop, mixinToMap);
        }

    }

    private static Class<?>[] getMixinParser(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(Parser.MixinParser.class))
        {
            Parser.MixinParser mixinParser = AnnoUtil.getAnnotation(clazz,Parser.MixinParser.class);
            Class<?>[] cs = mixinParser.value();
            cs = cs.length > 0 ? cs : mixinParser.porters();
            return cs;
        } else
        {
            return new Class[0];
        }
    }


    /**
     * 对{@linkplain Parser}和{@linkplain Parser.parse}的处理
     *
     * @return 有其中一个注解，返回true；否则返回false。
     */
    boolean bindParserAndParse(Class<?> clazz, InnerContextBridge innerContextBridge, InNames inNames,
            BackableSeek backableSeek, boolean needCheckLoop,
            Map<Class, Set<_MixinPorter>> mixinToMap) throws FatalInitException
    {
        if (needCheckLoop)
        {
            checkLoopMixin(clazz, true, mixinToMap);//防止循环混入
        }
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
            String[] paramNames = parse.getParamNames();
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
                        for (String varName : paramNames)
                        {
                            BackableSeek.bindVarNameWithTypeId(inNames, varName, typeId);
                        }
                    }
                    if (seekType == BackableSeek.SeekType.Add_Bind || seekType == BackableSeek.SeekType
                            .Add_NotBind)
                    {
                        for (String varName : paramNames)
                        {
                            backableSeek.put(varName, typeId);
                        }
                    }
                }

            } else if (!"".equals(parserName) && inNames != null)
            {
                ITypeParser iTypeParser = typeParserStore.byId(parserName);
                if (iTypeParser != null)
                {
                    for (String varName : paramNames)
                    {
                        BackableSeek.bindVarNameWithTypeId(inNames, varName, iTypeParser.id());
                    }
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
            LOGGER.warn(e.getMessage(),e);
            return null;
        }
        String id = typeParser.id();
        if (!typeParserStore.contains(typeParser))
        {
            typeParserStore.putParser(typeParser);
        }
        return id;
    }

    void addCheckPassable(Map<Class<?>, CheckPassable> checkPassableMap,
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
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
    }


    static _MixinPorter[] getMixin(Class<?> clazz, Map<Class, Set<_MixinPorter>> mixinToMap)
    {
        List<_MixinPorter> list = new ArrayList<>(1);
        if (clazz.isAnnotationPresent(Mixin.class))
        {
            Mixin mixin = AnnoUtil.getAnnotation(clazz,Mixin.class);
            Class[] classes = mixin.value().length > 0 ? mixin.value() : mixin.porters();
            int k=-1;
            for (Class c : classes)
            {
                k++;
                MixinOnly mixinOnly = AnnoUtil.getAnnotation(c, MixinOnly.class);
                MixinTo mixinTo = AnnoUtil.getAnnotation(c, MixinTo.class);
                if (mixinTo != null && !mixinTo.enableMixin())
                {
                    LOGGER.debug("mixin not enable:[{}] to [{}]", c, clazz);
                    continue;
                }
                _MixinPorter mixinPorter = new _MixinPorter(c, null, mixinOnly != null && mixinOnly.override());
                if(list.contains(mixinPorter)){
                    LOGGER.debug("mixin ignore for duplicate:index={},[{}] to [{}]",k, c, clazz);
                    continue;
                }
                list.add(mixinPorter);
            }
        }

        if (isEnableMixinTo(clazz) && mixinToMap.containsKey(clazz))
        {
            Set<_MixinPorter> set = mixinToMap.get(clazz);
            list.addAll(set);
        }

        return list.toArray(new _MixinPorter[0]);
    }

    private static boolean isEnableMixinTo(Class<?> clazz)
    {
        PortIn portIn = AnnoUtil.getAnnotation(clazz, PortIn.class);
        return portIn != null && portIn.enableMixinTo();
    }


    /**
     * 检查循环混入
     *
     * @param root
     */
    private void checkLoopMixin(Class<?> root, boolean isMixinOrMixinParser,
            Map<Class, Set<_MixinPorter>> mixinToMap) throws FatalInitException
    {
        Set<Walk<Class>> walkedSet = new HashSet<>();
        String loopMsg = checkLoopMixin(root, walkedSet, isMixinOrMixinParser, mixinToMap);
        if (loopMsg != null)
        {
            throw new FatalInitException(loopMsg);
        }
    }

    /**
     * 检查循环混入
     *
     * @param from
     * @param walkedSet
     * @param isMixinOrMixinParser
     * @return
     * @throws FatalInitException
     */
    private String checkLoopMixin(Class<?> from, Set<Walk<Class>> walkedSet,
            boolean isMixinOrMixinParser, Map<Class, Set<_MixinPorter>> mixinToMap) throws FatalInitException
    {
        if (isMixinOrMixinParser)
        {
            _MixinPorter[] mixins = getMixin(from, mixinToMap);
            for (_MixinPorter minxinPorter : mixins)
            {
                Class to = minxinPorter.getClazz();
                Walk<Class> walk = new Walk<>(from, to);
                if (walkedSet.contains(walk))
                {
                    String msg = String.format("Loop %s:[%s]->[%s]", Mixin.class.getSimpleName(), from, to);
                    LOGGER.warn(msg);
                    return msg;
                } else
                {
                    walkedSet.add(walk);
                    String msg = checkLoopMixin(to, walkedSet, isMixinOrMixinParser, mixinToMap);
                    if (msg != null)
                    {
                        LOGGER.warn("Walk:[{}]->[{}]", from, to);
                        return msg;
                    }
                }
            }
        } else
        {
            Class<?>[] mixins = getMixinParser(from);
            for (Class<?> to : mixins)
            {
                Walk<Class> walk = new Walk<>(from, to);
                if (walkedSet.contains(walk))
                {
                    String msg = String
                            .format("Loop %s:[%s]->[%s]", Parser.MixinParser.class.getSimpleName(), from, to);
                    LOGGER.warn(msg);
                    return msg;
                } else
                {
                    walkedSet.add(walk);
                    String msg = checkLoopMixin(to, walkedSet, isMixinOrMixinParser, mixinToMap);
                    if (msg != null)
                    {
                        LOGGER.warn("Walk:[{}]->[{}]", from, to);
                        return msg;
                    }
                }
            }
        }
        return null;
    }
}
