package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;


/**
 * @author Created by https://github.com/CLovinr on 2017/12/13.
 */
public class AspectHandleOfPortInUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AspectHandleOfPortInUtil.class);

    enum State
    {
        BeforeInvokeOfMethodCheck,
        BeforeInvoke,
        Invoke,
        AfterInvoke,
        OnFinal
    }

    public static void invokeFinalListener_beforeFinal(WObject wObject)
    {
        Enumeration<WObject.IFinalListener> enumeration = wObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                WObject.IFinalListener listener = enumeration.nextElement();
                listener.beforeFinal(wObject);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void invokeFinalListener_onFinalException(WObject wObject, Throwable throwable)
    {
        Enumeration<WObject.IFinalListener> enumeration = wObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                WObject.IFinalListener listener = enumeration.nextElement();
                listener.onFinalException(wObject, throwable);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void invokeFinalListener_afterFinal(WObject wObject)
    {
        Enumeration<WObject.IFinalListener> enumeration = wObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                WObject.IFinalListener listener = enumeration.nextElement();
                listener.afterFinal(wObject);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    public static final Object tryDoHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
            Object failedObject)

    {
        boolean hasInvokedException = false;
        if (state == State.OnFinal)
        {
            invokeFinalListener_beforeFinal(wObject);
            if (failedObject instanceof Throwable)
            {
                invokeFinalListener_onFinalException(wObject, (Throwable) failedObject);
                hasInvokedException = true;
            }
        }
        try
        {
            try
            {
                Object rs = doHandle(state, wObject, funPort, returnObject, failedObject);
                if (!hasInvokedException && state == State.OnFinal)
                {
                    invokeFinalListener_afterFinal(wObject);
                }
                return rs;
            } catch (Throwable throwable)
            {
                if (!hasInvokedException && state == State.OnFinal)
                {
                    invokeFinalListener_onFinalException(wObject, throwable);
                }
                throw throwable;
            }
        } catch (OftenCallException e)
        {
            throw e;
        } catch (Throwable e)
        {
            throw new OftenCallException(e);
        }
    }

    public static final Object doHandle(State state, WObject wObject, PorterOfFun funPort, Object returnObject,
            Object failedObject) throws Throwable
    {
        //处理AspectFunOperation
        AspectOperationOfPortIn.Handle[] handles = funPort.getHandles();
        if (handles != null)
        {

            switch (state)
            {
                case BeforeInvokeOfMethodCheck:
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        handle.beforeInvokeOfMethodCheck(wObject, funPort);
                    }
                    break;
                case BeforeInvoke:
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        handle.beforeInvoke(wObject, funPort);
                    }
                    break;
                case Invoke:
                {
                    boolean hasInvoked = false;
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        if (handle.needInvoke(wObject, funPort, returnObject))
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
