package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectOperationOfNormal(handle = LogMethodInvoke.HandleImpl.class)
public @interface LogMethodInvoke
{
    class HandleImpl extends AspectOperationOfNormal.HandleAdapter
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(HandleImpl.class);

        @Override
        public boolean init(Annotation current, IConfigData configData, Object object, Method method) throws Exception
        {
            return true;
        }

        @Override
        public boolean preInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn)
        {
            if (LOGGER.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("\n\t\tat ").append(originObject.getClass().getName()).append(".");
                sb.append(originMethod.getName()).append("(");
                sb.append(originObject.getClass().getSimpleName());
                sb.append(".java:0)");
                invoker.putAttr("time", System.currentTimeMillis());
                LOGGER.debug("{}", sb);
            }
            return false;
        }

        @Override
        public Object onEnd(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                AspectOperationOfNormal.Invoker invoker, Object lastFinalReturn) throws Exception
        {
            if (LOGGER.isDebugEnabled())
            {
                long time = invoker.getAttr("time");
                StringBuilder sb = new StringBuilder();
                sb.append("\n\t\tat ").append(originObject.getClass().getName()).append(".");
                sb.append(originMethod.getName()).append("(");
                sb.append(originObject.getClass().getSimpleName());
                sb.append(".java:0)");
                sb.append("\tinvoke dt=");
                sb.append(System.currentTimeMillis() - time);
                sb.append("ms");
                LOGGER.debug("{}", sb);
            }
            return lastFinalReturn;
        }

        @Override
        public void onException(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                AspectOperationOfNormal.Invoker invoker, Object[] args,
                Throwable throwable) throws Throwable
        {
            if (LOGGER.isDebugEnabled())
            {
                long time = invoker.getAttr("time");
                StringBuilder sb = new StringBuilder();
                sb.append("\n\t\tat ").append(originObject.getClass().getName()).append(".");
                sb.append(originMethod.getName()).append("(");
                sb.append(originObject.getClass().getSimpleName());
                sb.append(".java:0)");
                sb.append("\tinvoke on exception dt=");
                sb.append(System.currentTimeMillis() - time);
                sb.append("ms");
                LOGGER.debug("{}", sb);
            }
        }
    }
}
