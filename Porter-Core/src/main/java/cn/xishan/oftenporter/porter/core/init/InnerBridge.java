package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.annotation.sth.CacheTool;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.TypeParserStore;
import cn.xishan.oftenporter.porter.simple.DefaultTypeParserStore;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/28.
 */
public class InnerBridge
{
    public final Map<String, Object> globalAutoSet;
    /**
     * 用于存储全局的类型转换绑定。
     */
    public final TypeParserStore globalParserStore;
    List<CheckPassable> allGlobalChecksTemp;
    public final CacheTool cacheTool;


    public InnerBridge()
    {
        this.globalAutoSet = new ConcurrentHashMap<>();
        this.globalParserStore = new DefaultTypeParserStore();
        this.allGlobalChecksTemp = new Vector<>();
        this.cacheTool = new CacheTool();
    }
}
