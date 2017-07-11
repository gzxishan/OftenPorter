package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.sth.CacheOne;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.simple.DefaultParamsSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

/**
 * <p>
 * 如果有两个类A和B，分别有一个变量String sex和int sex,并且使用同一个{@linkplain TypeTo}，当这两个变量的转换类型通过自动或手动的方式绑定好后，使用{@linkplain TypeTo
 * }来转换对象A时sex为String，B时sex为int,不会出现同名而导致的类型覆盖问题。
 * </p>
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/10.
 */
public class TypeTo
{

    private final Logger LOGGER;
    private InnerContextBridge innerContextBridge;
    private PortUtil portUtil;

    /**
     * @param innerContextBridge
     */
    public TypeTo(InnerContextBridge innerContextBridge)
    {
        this.innerContextBridge = innerContextBridge;
        LOGGER = LogUtil.logger(TypeTo.class);
        portUtil = new PortUtil();
    }

    private CacheOne getCache(Class<?> clazz) throws Exception
    {
        CacheOne cache = innerContextBridge.innerBridge.cacheTool.getCacheOne(clazz, innerContextBridge);
        return cache;
    }

    /**
     * 手动绑定。
     *
     * @param clazz     当前类
     * @param fieldName 在clazz中必须声明了该名称的变量。
     * @throws RuntimeException
     */
    public <T> void bind(Class<T> clazz, String fieldName, ITypeParser typeParser) throws RuntimeException
    {
        try
        {
            CacheOne cache = getCache(clazz);

            Field field = clazz.getDeclaredField(fieldName);


            boolean binded = false;

            Field[] fields = cache.getOne().neceObjFields;
            for (int i = 0; i < fields.length; i++)
            {
                Field f = fields[i];
                if (f.equals(field))
                {
                    cache.getOne().inNames.nece[i].typeParserId = typeParser.id();
                    binded = true;
                    break;
                }
            }

            if (!binded)
            {
                fields = cache.getOne().unneceObjFields;
                for (int i = 0; i < fields.length; i++)
                {
                    Field f = fields[i];
                    if (f.equals(field))
                    {
                        cache.getOne().inNames.unece[i].typeParserId = typeParser.id();
                        binded = true;
                        break;
                    }
                }
            }

            if (binded)
            {
                LOGGER.debug("bind [{}] with parser [{}](id={})", field, typeParser, typeParser.id());
                TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
                if (!typeParserStore.contains(typeParser))
                {
                    typeParserStore.putParser(typeParser);
                }
            } else
            {
                throw new RuntimeException("Field named '" + fieldName + "' not found in class '" + clazz + "'");
            }

        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T parse(Class<T> clazz, final JSONObject jsonObject) throws RuntimeException
    {
        try
        {

            CacheOne cache = getCache(clazz);
            Object object = portUtil
                    .paramDealOne(false, innerContextBridge.paramDealt, cache.getOne(), new ParamSource()
                    {
                        @Override
                        public Object getParam(String name)
                        {
                            return jsonObject.get(name);
                        }

                        @Override
                        public <T> T getNeceParam(String name, String errmsgOfEmpty)
                        {
                            return DefaultParamsSource.getNeceParamUtil(this,name,errmsgOfEmpty);
                        }

                        @Override
                        public <T> T getNeceParam(String name)
                        {
                            return DefaultParamsSource.getNeceParamUtil(this,name);
                        }

                        @Override
                        public void putNewParams(Map<String, ?> newParams)
                        {
                            jsonObject.fluentPutAll(newParams);
                        }

                        @Override
                        public Enumeration<String> paramNames()
                        {
                            return new EnumerationImpl<String>(jsonObject.keySet());
                        }

                        @Override
                        public Enumeration<Map.Entry<String, Object>> params()
                        {
                            EnumerationImpl<Map.Entry<String, Object>> enumeration = new EnumerationImpl(
                                    jsonObject.entrySet());
                            return enumeration;
                        }
                    }, innerContextBridge.innerBridge.globalParserStore);
            if (object instanceof ParamDealt.FailedReason)
            {
                ParamDealt.FailedReason reason = (ParamDealt.FailedReason) object;
                throw new RuntimeException(reason.desc());
            }
            return (T) object;
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> parse(Class<T> clazz, JSONArray jsonArray) throws RuntimeException
    {
        try
        {
            List<T> list = new ArrayList<>(jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++)
            {
                list.add(parse(clazz, jsonArray.getJSONObject(i)));
            }
            return list;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
