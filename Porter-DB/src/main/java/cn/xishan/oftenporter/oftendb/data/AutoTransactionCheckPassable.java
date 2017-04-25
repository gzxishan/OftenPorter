package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.*;


/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
class AutoTransactionCheckPassable implements CheckPassable
{
    private TransactionConfirm transactionConfirm;


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
                            Common.startTransaction(wObject,
                                    transactionConfirm.getDBHandleSource(wObject, type, checkHandle),
                                    transactionConfirm.getParamsGetter(wObject, type, checkHandle));
                        }
                    }
                } else if (type == DuringType.AFTER_METHOD && (checkHandle.abOption.abPortType == ABPortType
                        .FINAL_LAST || checkHandle.abOption.abPortType == ABPortType.BOTH_FIRST_LAST))
                {
                    Common.commitTransaction(wObject);
                } else if (type == DuringType.ON_METHOD_EXCEPTION)
                {
                    Common.rollbackTransaction(wObject);
                    checkHandle.failed(checkHandle.exCause);
                    return;
                }
            } catch (Exception e)
            {
                checkHandle.failed(e);
                return;
            }

        }
        checkHandle.next();
    }
}
