package cn.xishan.oftenporter.uibinder.core;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
class UIListenerAndCallback implements UIAttrGetter.Listener, PCallback
{
    private UINamesOccurStore uiNamesOccurStore;
    private PortMethod method;
    private String pathPrefix, tiedFun;


    public UIListenerAndCallback(UINamesOccurStore uiNamesOccurStore, PortMethod method, String pathPrefix,
            String tiedFun)
    {
        this.uiNamesOccurStore = uiNamesOccurStore;
        this.method = method;
        this.pathPrefix = pathPrefix;
        this.tiedFun = tiedFun;
    }

    @Override
    public void onResponse(PResponse lResponse)
    {
        Object obj = lResponse.getResponse();
        if (obj != null && (obj instanceof JResponse))
        {
            JResponse jr = (JResponse) obj;
            if (!jr.isSuccess())
            {
                ErrListener errListener = uiNamesOccurStore.uiProvider.getErrListener();
                if (errListener != null)
                {
                    BinderData
                            binderData =
                            errListener.onErr(jr, pathPrefix, tiedFun);
                    if (binderData != null)
                    {
                        uiNamesOccurStore.doResponse(pathPrefix, binderData.toResponse());
                    }
                }

            } else
            {
                uiNamesOccurStore.doResponse(pathPrefix, jr);
            }
        }
    }


    @Override
    public void onGet(AppValues appValues)
    {
        UIProvider uiProvider = uiNamesOccurStore.uiProvider;
        uiProvider.getDelivery().currentBridge().request(
                new PRequest(method, pathPrefix + tiedFun).addParamAll(appValues), this);
    }
}
