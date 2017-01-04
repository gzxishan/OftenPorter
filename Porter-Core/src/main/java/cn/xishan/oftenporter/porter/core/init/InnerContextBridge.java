package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.ParamDealt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/28.
 */
public class InnerContextBridge
{
    public final ClassLoader classLoader;
    public final Map<String, Object> contextAutoSet;
    public final Map<String, Class<?>> autoGenImplMap;

    public final boolean responseWhenException;
    /**
     * 存放类和函数上实例化的检测对象。
     */
    public  Map<Class<?>, CheckPassable> checkPassableForCFTemp;
    public final InnerBridge innerBridge;
    public final AnnotationDealt annotationDealt;
    public final ParamDealt paramDealt;

    public InnerContextBridge(ClassLoader classLoader, InnerBridge innerBridge, Map<String, Object> contextAutoSet,
            Map<String, Class<?>> autoGenImplMap,
            boolean enableDefaultValue,PorterBridge porterBridge,boolean responseWhenException)
    {
        this.classLoader = classLoader;
        this.innerBridge = innerBridge;
        this.contextAutoSet = contextAutoSet;
        this.autoGenImplMap = autoGenImplMap;
        this.annotationDealt = new AnnotationDealt(enableDefaultValue);
        this.checkPassableForCFTemp = new HashMap<>();
        this.paramDealt=porterBridge.paramDealt();
        this.responseWhenException=responseWhenException;
    }
}
