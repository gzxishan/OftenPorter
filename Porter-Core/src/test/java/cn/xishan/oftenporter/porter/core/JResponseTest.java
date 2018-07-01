package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;
import org.junit.Test;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class JResponseTest
{
    @Test
    public void testToString()
    {
        JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
        ParamDealt.FailedReason failedReason = DefaultFailedReason.illegalParams("age");
        jResponse.setResult(failedReason.toJSON());
        LogUtil.printErrPosLn(jResponse);
    }
}
