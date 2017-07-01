package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.Configed;

/**
 * @author Created by https://github.com/CLovinr on 2017/7/1.
 */
class ConfigedImpl implements Configed
{
    private Object unit;

    @Override
    public Object getUnit()
    {
        return unit;
    }

    @Override
    public void setUnit(Object unit)
    {
        this.unit = unit;
    }
}
