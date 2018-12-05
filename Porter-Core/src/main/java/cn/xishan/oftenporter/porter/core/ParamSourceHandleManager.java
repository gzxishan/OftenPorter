package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.advanced.ParamSourceHandle;
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

    private Map<String, ParamSourceHandle> classTiedsMap = new HashMap<>();
    private Map<PortMethod, ParamSourceHandle> methodMap = new HashMap<>();


    /**
     * 通过接口类的绑定名来绑定。优先于{@linkplain #addByMethod(ParamSourceHandle, PortMethod...)}
     *
     * @param handle
     * @param classTieds
     */
    public void addByName(ParamSourceHandle handle, String... classTieds)
    {
        for (int i = 0; i < classTieds.length; i++)
        {
            classTiedsMap.put(classTieds[i], handle);
        }
    }

    /**
     * 通过请求方法来绑定。
     *
     * @param handle
     * @param methods
     */
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
     * 优先于{@linkplain #fromMethod(PortMethod)}.
     *
     * @param classTied 对应与顶层{@linkplain PortIn}的Class的绑定名。
     * @return
     */
    public ParamSourceHandle fromName(String classTied)
    {
        return classTiedsMap.get(classTied);
    }
}
