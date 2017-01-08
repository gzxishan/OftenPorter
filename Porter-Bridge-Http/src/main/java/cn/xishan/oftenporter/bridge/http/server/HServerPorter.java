package cn.xishan.oftenporter.bridge.http.server;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortOut;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
@PortIn(value = "HServer", tiedType = TiedType.REST)
abstract class HServerPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HServerPorter.class);
    @AutoSet("hServerDelivery")
    Delivery delivery;

    @PortIn(tiedType = TiedType.REST, method = PortMethod.GET)
    @PortOut(OutType.NoResponse)
    public void get(WObject wObject)
    {
        delivery(wObject);
    }

    @PortIn(tiedType = TiedType.REST, method = PortMethod.POST)
    @PortOut(OutType.NoResponse)
    public void post(WObject wObject)
    {
        delivery(wObject);
    }

    @PortIn(tiedType = TiedType.REST, method = PortMethod.PUT)
    @PortOut(OutType.NoResponse)
    public void put(WObject wObject)
    {
        delivery(wObject);
    }

    @PortIn(tiedType = TiedType.REST, method = PortMethod.DELETE)
    @PortOut(OutType.NoResponse)
    public void delete(WObject wObject)
    {
        delivery(wObject);
    }

    private void delivery(final WObject wObject)
    {
        delivery.toAllBridge().request(new PRequest(wObject.getRequest(), wObject.restValue), lResponse ->
        {
            if (lResponse != null)
            {
                try
                {
                    wObject.getResponse().write(lResponse.getResponse());
                } catch (IOException e)
                {
                    LOGGER.warn(e.getMessage(), e);
                } finally
                {
                    try
                    {
                        wObject.getResponse().close();
                    } catch (IOException e)
                    {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
            }
        });
    }
}
