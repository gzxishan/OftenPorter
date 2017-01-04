package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
class DeliveryImpl implements Delivery
{
    private Delivery delivery;
    private WObject wObject;
    private PBridgeImpl toAll, current;


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


    public DeliveryImpl(Delivery delivery, WObject wObject)
    {
        this.delivery = delivery;
        this.wObject = wObject;
    }

    @Override
    public PBridge currentBridge()
    {
        if (current == null)
        {
            synchronized (this)
            {
                if (current == null)
                {
                    current = new PBridgeImpl(delivery.currentBridge());
                }
            }
        }
        return current;
    }

    @Override
    public PBridge toAllBridge()
    {
        if (toAll == null)
        {
            synchronized (this)
            {
                if (toAll == null)
                {
                    toAll = new PBridgeImpl(delivery.toAllBridge());
                }
            }
        }
        return current;
    }

    @Override
    public PName currentPName()
    {
        return delivery.currentPName();
    }
}
