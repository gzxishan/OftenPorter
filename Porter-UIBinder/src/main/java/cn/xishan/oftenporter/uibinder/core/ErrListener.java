package cn.xishan.oftenporter.uibinder.core;


import cn.xishan.oftenporter.porter.core.JResponse;

import java.io.Serializable;

/**
 * Created by ZhuiFeng on 2015/6/12.
 */
public interface ErrListener extends Serializable{
    /**
     * @param jResponse
     * @param pathPrefix
     * @param tiedFun
     * @return 用于操作控件
     */
    BinderData onErr(JResponse jResponse, String pathPrefix, String tiedFun);
     void onException(Throwable throwable, String pathPrefix);
}
