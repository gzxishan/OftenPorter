package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.TypeParserStore;
import cn.xishan.oftenporter.porter.simple.parsers.ObjectParser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
public class DefaultTypeParserStore implements TypeParserStore
{
    private Map<String, ITypeParser> map = new ConcurrentHashMap<>();


    public DefaultTypeParserStore()
    {
        putParser(new ObjectParser());
    }

    @Override
    public ITypeParser byId(String id)
    {
        if (id == null)
        {
            return null;
        }
        return map.get(id);
    }

    @Override
    public void putParser(ITypeParser typeParser)
    {
        String id = typeParser.id();
        if (id == null)
        {
            throw new NullPointerException(typeParser.getClass().getName() + ".id() is null!");
        }
        map.put(id, typeParser);
    }

    @Override
    public boolean contains(ITypeParser typeParser)
    {
        String id = typeParser.id();
        if (id == null)
        {
            throw new NullPointerException(typeParser.getClass().getName() + ".id() is null!");
        }
        return map.containsKey(id);
    }
}
