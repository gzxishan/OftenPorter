package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class CacheTool
{

    private OftenEntitiesDeal OftenEntitiesDeal;

    public CacheTool()
    {
        OftenEntitiesDeal = new OftenEntitiesDeal();
    }

    private final Map<Class<?>, CacheOne> CACHES = new ConcurrentHashMap<>();

    /**
     * 添加到缓存中。
     *
     * @param clazz
     * @param cacheOne
     */
    void put(Class<?> clazz, CacheOne cacheOne)
    {
        CACHES.put(clazz, cacheOne);
    }

    public CacheOne getCacheOne(Class<?> clazz, InnerContextBridge innerContextBridge,boolean notFoundTypeParserThrows) throws Exception
    {
        CacheOne cacheOne = CACHES.get(clazz);
        if (cacheOne == null)
        {
            One one = OftenEntitiesDeal.bindOne(clazz, innerContextBridge,notFoundTypeParserThrows);
            cacheOne = new CacheOne(one);
            put(clazz, cacheOne);
        }
        return cacheOne;
    }
}
