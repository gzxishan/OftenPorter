package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;


/**
 * 抛出异常时才会进行事物回滚操作。
 *
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
class AutoTransactionCheckPassable implements CheckPassable
{
    private TransactionConfirm transactionConfirm;

    Logger LOGGER = LogUtil.logger(getClass());

    public AutoTransactionCheckPassable(TransactionConfirm transactionConfirm)
    {
        this.transactionConfirm = transactionConfirm;
    }

    @Override
    public void willPass(WObject wObject, DuringType type, CheckHandle checkHandle)
    {

        try
        {

            if (type == DuringType.ON_METHOD)
            {
                if (checkHandle.abOption.isFirst())
                {
                    if (transactionConfirm.needTransaction(wObject, type, checkHandle))
                    {
                        LOGGER.debug("transaction starting...({})", wObject.url());
                        TransactionConfirm.TConfig tConfig = transactionConfirm.getTConfig(wObject, type, checkHandle);
                        DBCommon.startTransaction(wObject, tConfig.dbSource, tConfig.transactionConfig);
                        LOGGER.debug("transaction started!");
                    }
                }
            } else if (type == DuringType.AFTER_METHOD)
            {
                if (checkHandle.abOption.isLast())
                {
                    if (DBCommon.commitTransaction(wObject))
                    {
                        LOGGER.debug("transaction committed!then closing...");
                        DBCommon.closeTransaction(wObject);
                        LOGGER.debug("transaction closed!({})", wObject.url());
                    }
                }

            } else if (type == DuringType.ON_METHOD_EXCEPTION)
            {
                if (DBCommon.rollbackTransaction(wObject))
                {
                    LOGGER.debug("transaction rollbacked!then closing...");
                    DBCommon.closeTransaction(wObject);
                    LOGGER.debug("transaction closed!({})", wObject.url());
                }
                checkHandle.failed(checkHandle.exCause);
                return;
            }
        } catch (Exception e)
        {
            LOGGER.debug("need close for some exception...");
            DBCommon.closeTransaction(wObject);
            LOGGER.debug("transaction closed in catch!({})", wObject.url());
            checkHandle.failed(e);
            return;
        }

        checkHandle.next();
    }
}
