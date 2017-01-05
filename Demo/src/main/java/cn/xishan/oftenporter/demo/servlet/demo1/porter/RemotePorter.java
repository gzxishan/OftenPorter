package cn.xishan.oftenporter.demo.servlet.demo1.porter;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

@PortIn
public class RemotePorter
{
    @PortIn
    public JResponse hello()
    {
	JResponse jResponse = new JResponse(ResultCode.SUCCESS);
	jResponse.setResult("HelloWorld");
	return jResponse;
    }
}
