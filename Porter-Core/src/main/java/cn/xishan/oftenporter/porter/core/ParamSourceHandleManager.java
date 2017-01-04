package cn.xishan.oftenporter.porter.core;



import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.ParamSourceHandle;
import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     首先调用{@linkplain #fromName(String)},
 *     若未找到则调用{@linkplain #fromMethod(PortMethod)},若还未找到则使用默认的。
 *     非线程安全。
 * </pre>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class ParamSourceHandleManager
{

    private Map<String, ParamSourceHandle> nameMap = new HashMap<>();
    private Map<PortMethod, ParamSourceHandle> methodMap = new HashMap<>();

    public void addByName(ParamSourceHandle handle, String... names)
    {
        for (int i = 0; i < names.length; i++)
        {
            nameMap.put(names[i], handle);
        }
    }

    public void addByMethod(ParamSourceHandle handle, PortMethod... methods)
    {
        for (int i = 0; i < methods.length; i++)
        {
            methodMap.put(methods[i], handle);
        }
    }

    public ParamSourceHandle fromMethod(PortMethod method)
    {
        return methodMap.get(method);
    }

    /**
     * @param name 对应与顶层{@linkplain PortIn}的绑定名。
     * @return
     */
    public ParamSourceHandle fromName(String name)
    {
        return nameMap.get(name);
    }
}
