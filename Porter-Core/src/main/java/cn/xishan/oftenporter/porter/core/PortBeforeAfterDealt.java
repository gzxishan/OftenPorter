package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortFilterOne;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.ABOption;
import cn.xishan.oftenporter.porter.core.base.ABPortType;
import cn.xishan.oftenporter.porter.core.base.ABType;
import cn.xishan.oftenporter.porter.core.base.PortFunReturn;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;

/**
 * Created by chenyg on 2017-04-17.
 */
class PortBeforeAfterDealt
{

    enum DoState{
        DoInvoke,DoMethodCheck,DoAfter,DoResponse
    }

    interface Callback<T>
    {
        void onCall(boolean isOk, T t);
    }


    private PorterOfFun funPort;
    private WObjectImpl wObject;
    private Object object;
    private Callback<Object> callbackForReturn;


    public PortBeforeAfterDealt(WObjectImpl wObject, PorterOfFun funPort)
    {
        this.funPort = funPort;
        this.wObject = wObject;
    }

    public void startBefore(Callback<Object> callbackForReturn)
    {
        this.callbackForReturn = callbackForReturn;

        doBefore(0,ABPortType.ORIGIN_FIRST);
    }



    private void doBefore(int index,ABPortType abPortType)
    {
        doBefore(index,abPortType, (isOk, isFinish) ->
        {
            if (isFinish && isOk)
            {
                callbackForReturn.onCall(true, object);
            } else if (isOk)
            {
                doBefore(index + 1,ABPortType.OTHER);
            } else
            {
                callbackForReturn.onCall(false, object);
            }
        });
    }

    private void doBefore(int index,ABPortType abPortType, Callback<Boolean> callback)
    {
        _PortFilterOne[] portBefores = funPort.getPortBefores();

        _PortFilterOne portBefore = portBefores[index];
        PRequest request = PRequest
                .withNewPath(portBefore.getPathWithContext(), portBefore.getMethod(), wObject.getRequest(), false);
        ABOption abOption = new ABOption(wObject._otherObject, ABType.METHOD_OF_BEFORE,abPortType);
        request._setABOption_(abOption);

        wObject.delivery().currentBridge().request(request, lResponse ->
        {
            object = lResponse.getResponse();
            if (lResponse.isOk())
            {
                mayPutParams();
            }
            callback.onCall(lResponse.isOk(), index >= funPort.getPortBefores().length - 1);
        });

    }

    private void mayPutParams()
    {
        if (object == null)
        {
            return;
        }
        if (object instanceof PortFunReturn)
        {
            PortFunReturn portFunReturn = (PortFunReturn) object;
            wObject.url().putNewParams(portFunReturn.getParams());
            object = null;
        }
    }


    public void startAfter(Callback<Object> callbackForReturn)
    {
        this.callbackForReturn = callbackForReturn;
        doAfter(0,ABPortType.FINAL_LAST);
    }

    private void doAfter(int index,ABPortType abPortType)
    {

        doAfter(index,abPortType, (isOk, isFinish) ->
        {
            if (isFinish && isOk)
            {
                callbackForReturn.onCall(true, object);
            } else if (isOk)
            {
                doAfter(index + 1,ABPortType.OTHER);
            } else
            {
                callbackForReturn.onCall(false, object);
            }
        });

    }

    private void doAfter(int index,ABPortType abPortType, Callback<Boolean> callback)
    {
        _PortFilterOne[] portAfters = funPort.getPortAfters();

        _PortFilterOne portAfter = portAfters[index];
        PRequest request = PRequest
                .withNewPath(portAfter.getPathWithContext(), portAfter.getMethod(), wObject.getRequest(), false);
        ABOption abOption = new ABOption(wObject._otherObject, ABType.METHOD_OF_AFTER,abPortType);
        request._setABOption_(abOption);

        wObject.delivery().currentBridge().request(request, lResponse ->
        {
            object = lResponse.getResponse();
            boolean isFinish = index >= funPort.getPortAfters().length - 1;
            if (!isFinish && lResponse.isOk())
            {
                mayPutParams();
            }
            callback.onCall(lResponse.isOk(), isFinish);
        });

    }

}
