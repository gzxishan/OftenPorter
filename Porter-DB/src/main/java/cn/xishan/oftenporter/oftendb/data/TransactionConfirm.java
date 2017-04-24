package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public interface TransactionConfirm
{
    boolean needTransaction(WObject wObject, DuringType type, CheckHandle checkHandle);

    DBHandleSource getDBHandleSource(WObject wObject, DuringType type, CheckHandle checkHandle);

    ParamsGetter getParamsGetter(WObject wObject, DuringType type, CheckHandle checkHandle);
}
