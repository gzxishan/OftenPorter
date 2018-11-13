package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
class DeliveryImpl implements Delivery
{
    private BridgeLinker bridgeLinker;
    private OftenObject oftenObject;
    private IBridgeImpl toAll, current, inner;


    public class IBridgeImpl implements IBridge
    {
        private IBridge iBridge;

        public IBridgeImpl(IBridge iBridge)
        {
            this.iBridge = iBridge;
        }

        @Override
        public void request(BridgeRequest request, BridgeCallback callback)
        {
            iBridge.request(new BridgeRequestWithSource(request, oftenObject), callback);
        }
    }


    public DeliveryImpl(BridgeLinker bridgeLinker, OftenObject oftenObject)
    {
        this.bridgeLinker = bridgeLinker;
        this.oftenObject = oftenObject;
    }

    @Override
    public synchronized IBridge currentBridge()
    {
        if (current == null)
        {
            current = new IBridgeImpl(bridgeLinker.currentBridge());
        }
        return current;
    }

    @Override
    public synchronized IBridge innerBridge()
    {
        if (inner == null)
        {
            inner = new IBridgeImpl(bridgeLinker.innerBridge());
        }
        return inner;
    }

    @Override
    public synchronized IBridge toAllBridge()
    {
        if (toAll == null)
        {
            toAll = new IBridgeImpl(bridgeLinker.toAllBridge());
        }
        return toAll;
    }

    @Override
    public BridgeName currentName()
    {
        return bridgeLinker.currentName();
    }
}
