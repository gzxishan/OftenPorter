package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.advanced.IAutoSetListener;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/28.
 */
public class InnerContextBridge
{
    public final ClassLoader classLoader;
    private final Map<String, Object> contextAutoSet;

    public final boolean responseWhenException;
    /**
     * 存放类和函数上实例化的检测对象。
     */
    public Map<Class<?>, CheckPassable> checkPassableForCFTemps;
    public final InnerBridge innerBridge;
    public final AnnotationDealt annotationDealt;
    public final ParamDealt paramDealt;

    public final OutType defaultOutType;

    public final IAutoSetListener[] autoSetListeners;

    public InnerContextBridge(ClassLoader classLoader, InnerBridge innerBridge, Map<String, Object> contextAutoSet,
            boolean enableDefaultValue, PorterBridge porterBridge, OutType defaultOutType,
            IAutoSetListener[] autoSetListeners,
            boolean responseWhenException)
    {
        this.classLoader = classLoader;
        this.innerBridge = innerBridge;
        this.contextAutoSet = contextAutoSet;
        this.annotationDealt = AnnotationDealt.newInstance(enableDefaultValue);
        this.checkPassableForCFTemps = new HashMap<>();
        this.paramDealt = porterBridge.paramDealt();
        this.defaultOutType = defaultOutType;
        this.autoSetListeners = autoSetListeners;
        this.responseWhenException = responseWhenException;
    }

    public Map<String, Object> getContextAutoSetMap()
    {
        return contextAutoSet;
    }

    public Object getContextSet(String objectName)
    {
        return contextAutoSet.get(objectName);
    }

    public Object putContextSet(String objectName, Object object)
    {
        Object last=null;
        if (object != null)
        {
           last= contextAutoSet.put(objectName, object);
            String autoSetName = AnnoUtil.getAutoSetName(object);
            if (OftenTool.notEmpty(autoSetName))
            {
                contextAutoSet.put(autoSetName, object);
            }
        }
        return last;
    }
}
