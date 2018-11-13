package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
abstract class DeliveryBuilder
{
    public abstract Delivery build(OftenObject oftenObject);

    /**
     * @param withSth 是否带上对方的消息。
     * @return
     */
    public static DeliveryBuilder getBuilder(boolean withSth, final BridgeLinker bridgeLinker)
    {
        if (withSth)
        {
            return new DeliveryBuilder()
            {
                @Override
                public Delivery build(OftenObject oftenObject)
                {
                    return new DeliveryImpl(bridgeLinker, oftenObject);
                }
            };
        } else
        {
            return new DeliveryBuilder()
            {
                @Override
                public Delivery build(OftenObject oftenObject)
                {
                    return bridgeLinker;
                }
            };
        }
    }
}
