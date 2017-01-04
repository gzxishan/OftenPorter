package cn.xishan.oftenporter.uibinder.core;

import cn.xishan.oftenporter.porter.core.base.AppValues;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/2.
 */
public interface UIAttrGetter
{
    interface Listener
    {
        void onGet(AppValues appValues);
    }

    void onGet(Listener listener,String tiedFun, Binder[] binders, AttrEnum... types);
}
