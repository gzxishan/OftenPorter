package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;


/**
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
        if (checkHandle.handleMethod != null)
        {
            try
            {
                if (type == DuringType.ON_METHOD)
                {
                    if (checkHandle.abOption.abPortType == ABPortType.ORIGIN_FIRST || checkHandle.abOption.abPortType
                            == ABPortType.BOTH_FIRST_LAST)
                    {
                        if (transactionConfirm.needTransaction(wObject, type, checkHandle))
                        {
                            LOGGER.debug("transaction starting...({})",wObject.url());
                            Common.startTransaction(wObject,
                                    transactionConfirm.getDBHandleSource(wObject, type, checkHandle),
                                    transactionConfirm.getParamsGetter(wObject, type, checkHandle));
                            LOGGER.debug("transaction started!");
                        }
                    }
                } else if (type == DuringType.AFTER_METHOD && (checkHandle.abOption.abPortType == ABPortType
                        .FINAL_LAST || checkHandle.abOption.abPortType == ABPortType.BOTH_FIRST_LAST))
                {
                    if (Common.commitTransaction(wObject))
                    {
                        LOGGER.debug("transaction committed!then closing...");
                        Common.closeTransaction(wObject);
                        LOGGER.debug("transaction closed!({})",wObject.url());
                    }
                } else if (type == DuringType.ON_METHOD_EXCEPTION)
                {
                    if (Common.rollbackTransaction(wObject))
                    {
                        LOGGER.debug("transaction rollbacked!then closing...");
                        Common.closeTransaction(wObject);
                        LOGGER.debug("transaction closed!({})",wObject.url());
                    }
                    checkHandle.failed(checkHandle.exCause);
                    return;
                }
            } catch (Exception e)
            {
                LOGGER.debug("need close for some exception...");
                Common.closeTransaction(wObject);
                LOGGER.debug("transaction closed in catch!({})",wObject.url());
                checkHandle.failed(e);
                return;
            }

        }
        checkHandle.next();
    }
}
