package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
abstract class DeliveryBuilder
{
    public abstract Delivery build(WObject wObject);

    /**
     * @param withSth 是否带上对方的消息。
     * @return
     */
    public static DeliveryBuilder getBuilder(boolean withSth, final Delivery delivery)
    {
        if (withSth)
        {
            return new DeliveryBuilder()
            {
                @Override
                public Delivery build(WObject wObject)
                {
                    return new DeliveryImpl(delivery, wObject);
                }
            };
        } else
        {
            return new DeliveryBuilder()
            {
                @Override
                public Delivery build(WObject wObject)
                {
                    return delivery;
                }
            };
        }
    }
}
