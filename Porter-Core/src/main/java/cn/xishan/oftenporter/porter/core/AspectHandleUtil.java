package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/13.
 */
class AspectHandleUtil
{
    enum State
    {
        BeforeInvokeOfMethodCheck,
        BeforeInvoke,
        Invoke,
        AfterInvoke,
        OnFinal
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectHandleUtil.class);

    public static Object tryDoHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
            Object failedObject)

    {
        try
        {
            return doHandle(state, wObject, funPort, returnObject, failedObject);
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public static Object doHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
            Object failedObject) throws Exception
    {
        //处理AspectFunOperation
        AspectFunOperation.Handle[] handles = funPort.getHandles();
        if (handles != null)
        {

            switch (state)
            {
                case BeforeInvokeOfMethodCheck:
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        handle.beforeInvokeOfMethodCheck(wObject, funPort);
                    }
                    break;
                case BeforeInvoke:
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        handle.beforeInvoke(wObject, funPort);
                    }
                    break;
                case Invoke:
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        returnObject = handle.invoke(wObject, funPort, returnObject);
                    }
                    return returnObject;
                case AfterInvoke:
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        handle.afterInvoke(wObject, funPort, returnObject);
                    }
                    break;

                case OnFinal:
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        handle.onFinal(wObject, funPort, returnObject, failedObject);
                    }
                    break;
            }

        }
        return null;
    }
}
