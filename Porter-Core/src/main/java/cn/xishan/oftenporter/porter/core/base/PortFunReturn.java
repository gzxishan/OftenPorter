package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.After;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于与{@linkplain After After}和{@linkplain Before Before}进行配合。
 * <pre>
 *     1.当直接返回时，会变成{@linkplain JResponse JResponse}的结果。
 *     2.作为中间过滤器时，会变成参数。
 * </pre>
 * Created by chenyg on 2017-04-17.
 */
public class PortFunReturn
{
    private Map<String, Object> params;

    public PortFunReturn(Map<String, Object> params)
    {
        this.params = params;
    }

    public PortFunReturn()
    {
        this(new HashMap<>());
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public PortFunReturn put(String name, Object value)
    {
        params.put(name, value);
        return this;
    }

    @Override
    public String toString()
    {
        return params == null ? "null" : params.toString();
    }
}
