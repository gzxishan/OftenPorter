package cn.xishan.oftenporter.demo.core.test4.porter;

import cn.xishan.oftenporter.demo.core.test4.sth.ID;
import cn.xishan.oftenporter.demo.core.test4.sth.User;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntities;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;

@PortIn
@BindEntities(ID.class)
public class BindSth2Porter
{
    /**
     * <pre>
     * 1.通过method指定请求方法。
     * 2.通过@PortInObj来绑定对象，对象中的字段通过@InNece来指定是必需值，@InUnNece指定非必需值，默认情况下变量的类型是
     * 自动绑定的。
     * </pre>
     * 
     * @param wObject
     * @return
     */

    @PortIn(method = PortMethod.POST)
    @BindEntities({ User.class })
    public Object send(WObject wObject)
    {
	ID id = wObject.centity(0);
	User user = wObject.fentity(0);
	return user + "," + id;
    }

}
