package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.*;

/**
 * 设置禁止http缓存。
 *
 * @author Created by https://github.com/CLovinr on 2018/12/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@AspectOperationOfPortIn(handle = NoCache.HandleImpl.class)
public @interface NoCache
{
    boolean noCache() default true;

    class HandleImpl extends AspectOperationOfPortIn.HandleAdapter<NoCache>
    {
        private boolean init(NoCache noCache)
        {
            return noCache.noCache();
        }

        @Override
        public boolean init(NoCache current, IConfigData configData, Porter porter)
        {
            return init(current);
        }

        @Override
        public boolean init(NoCache current, IConfigData configData, PorterOfFun porterOfFun)
        {
            return init(current);
        }

        @Override
        public void beforeInvoke(OftenObject oftenObject, PorterOfFun porterOfFun)
        {
            if (oftenObject.getRequest().getOriginalResponse() instanceof HttpServletResponse)
            {
                HttpServletResponse response = oftenObject.getRequest().getOriginalResponse();
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("expires", -1);
            }
        }

        @Override
        public boolean needInvoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn)
        {
            return false;
        }
    }
}
