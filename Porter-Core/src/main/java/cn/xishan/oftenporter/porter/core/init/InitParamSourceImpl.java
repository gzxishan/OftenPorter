package cn.xishan.oftenporter.porter.core.init;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by 宇宙之灵 on 2016/8/31.
 */
abstract class InitParamSourceImpl implements InitParamSource
{
    private Map<String, Object> map = new HashMap<>();

    public InitParamSourceImpl()
    {
    }

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
