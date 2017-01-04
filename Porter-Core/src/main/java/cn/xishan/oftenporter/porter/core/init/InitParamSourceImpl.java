package cn.xishan.oftenporter.porter.core.init;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 宇宙之灵 on 2016/8/31.
 */
class InitParamSourceImpl implements InitParamSource
{
    private Map<String, Object> map = new HashMap<>();

    @Override
    public Object getInitParameter(String name)
    {
        return map.get(name);
    }

    @Override
    public void putInitParameter(String name, Object value)
    {
        map.put(name, value);
    }
}
