package cn.xishan.oftenporter.bridge.http.server;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortOut;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
@PortIn(value = "HServer", tiedType = TiedType.METHOD)
abstract class HServerPorter
{
    private static final Logger LOGGER = LogUtil.logger(HServerPorter.class);
    @AutoSet("hServerDelivery")
    Delivery delivery;

    @PortIn(tiedType = TiedType.METHOD, method = PortMethod.GET)
    @PortOut(OutType.NO_RESPONSE)
    public void get(OftenObject oftenObject)
    {
        delivery(oftenObject);
    }

    @PortIn(tiedType = TiedType.METHOD, method = PortMethod.POST)
    @PortOut(OutType.NO_RESPONSE)
    public void post(OftenObject oftenObject)
    {
        delivery(oftenObject);
    }

    @PortIn(tiedType = TiedType.METHOD, method = PortMethod.PUT)
    @PortOut(OutType.NO_RESPONSE)
    public void put(OftenObject oftenObject)
    {
        delivery(oftenObject);
    }

    @PortIn(tiedType = TiedType.METHOD, method = PortMethod.DELETE)
    @PortOut(OutType.NO_RESPONSE)
    public void delete(OftenObject oftenObject)
    {
        delivery(oftenObject);
    }

    private void delivery(final OftenObject oftenObject)
    {
        delivery.toAllBridge().request(new BridgeRequest(oftenObject, oftenObject.funTied()), lResponse ->
        {
            if (lResponse != null)
            {
                try
                {
                    oftenObject.getResponse().write(lResponse.getResponse());
                } catch (IOException e)
                {
                    LOGGER.warn(e.getMessage(), e);
                } finally
                {
                    try
                    {
                        oftenObject.getResponse().close();
                    } catch (IOException e)
                    {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
            }
        });
    }
}
