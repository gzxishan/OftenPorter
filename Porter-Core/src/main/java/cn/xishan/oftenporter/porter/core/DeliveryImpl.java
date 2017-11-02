package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
class DeliveryImpl implements Delivery
{
    private PLinker pLinker;
    private WObject wObject;
    private PBridgeImpl toAll, current, inner;


    public class PBridgeImpl implements PBridge
    {
        private PBridge pBridge;

        public PBridgeImpl(PBridge pBridge)
        {
            this.pBridge = pBridge;
        }

        @Override
        public void request(PRequest request, PCallback callback)
        {
            pBridge.request(new PRequestWithSource(request, wObject), callback);
        }
    }


    public DeliveryImpl(PLinker pLinker, WObject wObject)
    {
        this.pLinker = pLinker;
        this.wObject = wObject;
    }

    @Override
    public synchronized PBridge currentBridge()
    {
        if (current == null)
        {
            current = new PBridgeImpl(pLinker.currentBridge());
        }
        return current;
    }

    @Override
    public synchronized PBridge innerBridge()
    {
        if (inner == null)
        {
            inner = new PBridgeImpl(pLinker.innerBridge());
        }
        return inner;
    }

    @Override
    public synchronized PBridge toAllBridge()
    {
        if (toAll == null)
        {
            toAll = new PBridgeImpl(pLinker.toAllBridge());
        }
        return toAll;
    }

    @Override
    public PName currentPName()
    {
        return pLinker.currentPName();
    }
}
