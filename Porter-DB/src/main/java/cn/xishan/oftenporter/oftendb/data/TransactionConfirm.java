package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.TransactionConfig;
import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public interface TransactionConfirm
{
    class TConfig
    {
        public DBSource dbSource;
        public TransactionConfig transactionConfig;
    }

    boolean needTransaction(WObject wObject, DuringType type, CheckHandle checkHandle);

    TConfig getTConfig(WObject wObject, DuringType type, CheckHandle checkHandle);
}
