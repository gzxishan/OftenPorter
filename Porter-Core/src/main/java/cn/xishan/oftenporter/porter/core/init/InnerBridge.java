package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.CacheTool;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultTypeParserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放全局的成员。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/28.
 */
public class InnerBridge
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InnerBridge.class);
    private final Map<String, Object> globalAutoSet;
    /**
     * 用于存储全局的类型转换绑定。
     */
    public final TypeParserStore globalParserStore;
    List<CheckPassable> allGlobalChecksTemp;
    public final CacheTool cacheTool;

    public InnerBridge(String defaultTypeParserId)
    {
        this.globalAutoSet = new ConcurrentHashMap<>();
        DefaultTypeParserStore typeParserStore = new DefaultTypeParserStore();
        if (OftenTool.notEmpty(defaultTypeParserId))
        {
            typeParserStore.setDefaultTypeParser(defaultTypeParserId);
        }
        this.globalParserStore = typeParserStore;
        this.allGlobalChecksTemp = new Vector<>();
        this.cacheTool = new CacheTool();
    }

    public Map<String, Object> getGlobalAutoSet()
    {
        return globalAutoSet;
    }

    public Object getGlobalSet(String objectName)
    {
        return globalAutoSet.get(objectName);
    }

    public Object putGlobalSet(String objectName, Object object)
    {
        Object last = null;
        if (object != null)
        {
            last = globalAutoSet.put(objectName, object);
            if (last != null)
            {
                LOGGER.warn("the global object named '{}' added before [{}]", objectName, last);
            }
            String autoSetName = AnnoUtil.getAutoSetName(object);
            if (OftenTool.notEmpty(autoSetName))
            {
                Object obj = globalAutoSet.put(autoSetName, object);
                if (obj != null)
                {
                    LOGGER.warn("the global object named '{}' added before [{}]", autoSetName, last);
                }
            }
        }
        return last;
    }
}
