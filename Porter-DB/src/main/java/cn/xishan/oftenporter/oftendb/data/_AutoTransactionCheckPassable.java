package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;


/**
 * 抛出异常时才会进行事物回滚操作。
 *
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public class _AutoTransactionCheckPassable implements CheckPassable
{
    private TransactionConfirm transactionConfirm;

    Logger LOGGER = LogUtil.logger(getClass());

    public _AutoTransactionCheckPassable(TransactionConfirm transactionConfirm)
    {
        this.transactionConfirm = transactionConfirm;
    }

    @Override
    public void willPass(WObject wObject, DuringType type, CheckHandle checkHandle)
    {
        if (wObject.isTopRequest())
        {
            try
            {
                if (type == DuringType.ON_METHOD)
                {
                    if (transactionConfirm.needTransaction(wObject, type, checkHandle))
                    {
                        LOGGER.debug("transaction starting...({}:{})", wObject.url(),
                                wObject.getRequest().getMethod());
                        TransactionConfirm.TConfig tConfig = transactionConfirm.getTConfig(wObject, type, checkHandle);
                        DBCommon.startTransaction(wObject, tConfig.dbSource, tConfig.transactionConfig);
                        LOGGER.debug("transaction started!");
                    }
                } else if (type == DuringType.AFTER_METHOD)
                {

                    if (DBCommon.commitTransaction(wObject))
                    {
                        LOGGER.debug("transaction committed!then closing...({}:{})", wObject.url(),
                                wObject.getRequest().getMethod());
                        DBCommon.closeTransaction(wObject);
                        LOGGER.debug("transaction closed!");
                    }


                } else if (type == DuringType.ON_METHOD_EXCEPTION)
                {
                    if (DBCommon.rollbackTransaction(wObject))
                    {
                        LOGGER.debug("transaction rollbacked!then closing...({}:{})", wObject.url(),
                                wObject.getRequest().getMethod());
                        DBCommon.closeTransaction(wObject);
                        LOGGER.debug("transaction closed!");
                    }
                    checkHandle.failed(checkHandle.exCause);
                    return;
                }
            } catch (Exception e)
            {
                LOGGER.debug("need close for some exception...({}:{})", wObject.url(),
                        wObject.getRequest().getMethod());
                DBCommon.closeTransaction(wObject);
                LOGGER.debug("transaction closed in catch!");
                checkHandle.failed(e);
                return;
            }
        }

        checkHandle.next();
    }
}
