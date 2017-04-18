package cn.xishan.oftenporter.porter.local.porter3;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortFunReturn;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.Date;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/17.
 */
@PortIn("TestAB")
public class TestABPorter
{
    @PortIn(tied = "main", nece = {"time"})
    @PortIn.Filter(before = @PortIn.Before(funTied = "genData"))
    public Object main(WObject wObject)
    {
        Date time = wObject.fnOf(0);
        return time;
    }

    @PortIn(tied = "genData", portFunType = PortFunType.JUST_BEFORE_AFTER)
    public Object genData()
    {
        PortFunReturn funReturn = new PortFunReturn();
        funReturn.put("time", new Date());
        return funReturn;
    }
}
