package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortAfter;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortBefore;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.PortFunReturn;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;

/**
 * Created by chenyg on 2017-04-17.
 */
class PortBeforeAfterDealt
{

    enum DoState{
        DoBefore,DoInvoke,DoMethodCheck,DoAfter,DoResponse
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

    public void doBefore(Callback<Object> callbackForReturn)
    {
        this.callbackForReturn = callbackForReturn;
        doBefore(0);
    }

    public void doAfter(Callback<Object> callbackForReturn)
    {
        this.callbackForReturn = callbackForReturn;
        doBefore(0);
    }

    private void doBefore(int index)
    {
        doBefore(index, (isOk, isFinish) ->
        {
            if (isFinish && isOk)
            {
                callbackForReturn.onCall(true, object);
            } else if (isOk)
            {
                doBefore(index + 1);
            } else
            {
                callbackForReturn.onCall(false, object);
            }
        });
    }

    private void doBefore(int index, Callback<Boolean> callback)
    {
        _PortBefore[] portBefores = funPort.getPortBefores();

        _PortBefore portBefore = portBefores[index];
        PRequest request = PRequest
                .withNewPath(portBefore.getPathWithContext(), portBefore.getMethod(), wObject.getRequest(), false);
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

    private void doAfter(int index)
    {

        doAfter(index, (isOk, isFinish) ->
        {
            if (isFinish && isOk)
            {
                callbackForReturn.onCall(true, object);
            } else if (isOk)
            {
                doAfter(index + 1);
            } else
            {
                callbackForReturn.onCall(false, object);
            }
        });

    }

    private void doAfter(int index, Callback<Boolean> callback)
    {
        _PortAfter[] portAfters = funPort.getPortAfters();

        _PortAfter portAfter = portAfters[index];
        PRequest request = PRequest
                .withNewPath(portAfter.getPathWithContext(), portAfter.getMethod(), wObject.getRequest(), false);
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
