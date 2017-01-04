package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.porter.simple.SimpleAppValues;
import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.Binder;
import cn.xishan.oftenporter.uibinder.core.IdDeal;
import cn.xishan.oftenporter.uibinder.core.UIAttrGetter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
class UIAttrGetterImpl implements UIAttrGetter
{
    @Override
    public void onGet(Listener listener, String tiedFun, Binder[] binders, AttrEnum... types)
    {
        Object[] values = new Object[binders.length];
        String[] names = new String[binders.length];
        for (int i = 0; i < binders.length; i++)
        {
            Binder binder = binders[i];
            if (binder == null)
            {
                continue;
            }
            IdDeal.Result result = binder.getResult();
            Object value = binders[i].get(types.length == 1 ? types[0] : types[i]);

            names[i]=result.getVarName();
            values[i]=value;

        }
        listener.onGet(new SimpleAppValues(names).values(values));
    }
}
