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

    public static final Object tryDoHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
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

    public static final Object doHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
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
                {
                    boolean hasInvoked = false;
                    for (AspectFunOperation.Handle handle : handles)
                    {
                        if (handle.needInvoke(wObject,funPort,returnObject))
                        {
                            hasInvoked = true;
                            returnObject = handle.invoke(wObject, funPort, returnObject);
                        }
                    }
                    if (!hasInvoked)
                    {
                        returnObject = funPort.invokeByHandleArgs(wObject);
                    }
                    return returnObject;
                }

                case AfterInvoke:
                    for (int i = handles.length - 1; i >= 0; i--)
                    {
                        handles[i].afterInvoke(wObject, funPort, returnObject);
                    }
                    break;

                case OnFinal:
                    for (int i = handles.length - 1; i >= 0; i--)
                    {
                        handles[i].onFinal(wObject, funPort, returnObject, failedObject);
                    }
                    break;
            }

        }
        return null;
    }
}
