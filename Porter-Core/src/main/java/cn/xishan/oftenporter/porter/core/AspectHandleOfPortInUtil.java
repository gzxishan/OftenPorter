package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
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

    public enum State
    {
        BeforeInvokeOfMethodCheck,
        BeforeInvoke,
        Invoke,
        AfterInvoke,
        OnFinal
    }

    public static void invokeFinalListener_beforeFinal(OftenObject oftenObject)
    {
        Enumeration<OftenObject.IFinalListener> enumeration = oftenObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                OftenObject.IFinalListener listener = enumeration.nextElement();
                listener.beforeFinal(oftenObject);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void invokeFinalListener_onFinalException(OftenObject oftenObject, Throwable throwable)
    {
        Enumeration<OftenObject.IFinalListener> enumeration = oftenObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                OftenObject.IFinalListener listener = enumeration.nextElement();
                listener.onFinalException(oftenObject, throwable);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void invokeFinalListener_afterFinal(OftenObject oftenObject)
    {
        Enumeration<OftenObject.IFinalListener> enumeration = oftenObject.listeners(-1);
        while (enumeration.hasMoreElements())
        {
            try
            {
                OftenObject.IFinalListener listener = enumeration.nextElement();
                listener.afterFinal(oftenObject);
            } catch (Throwable e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    public static final Object tryDoHandle(State state, OftenObject oftenObject, PorterOfFun funPort,
            Object returnObject,
            Object failedObject)

    {
        boolean hasInvokedException = false;
        if (state == State.OnFinal)
        {
            invokeFinalListener_beforeFinal(oftenObject);
            if (failedObject instanceof Throwable)
            {
                invokeFinalListener_onFinalException(oftenObject, (Throwable) failedObject);
                hasInvokedException = true;
            }
        }
        try
        {
            try
            {
                Object rs = doHandle(state, oftenObject, funPort, returnObject, failedObject);
                if (!hasInvokedException && state == State.OnFinal)
                {
                    invokeFinalListener_afterFinal(oftenObject);
                }
                return rs;
            } catch (Throwable throwable)
            {
                if (!hasInvokedException && state == State.OnFinal)
                {
                    invokeFinalListener_onFinalException(oftenObject, throwable);
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

    public static final Object doHandle(State state, OftenObject oftenObject, PorterOfFun funPort, Object returnObject,
            Object failedObject) throws Throwable
    {
        return doHandle(state, oftenObject, funPort, returnObject, failedObject, false);
    }

    public static final Object doHandle(State state, OftenObject oftenObject, PorterOfFun funPort, Object returnObject,
            Object failedObject, boolean checkSupport) throws Throwable
    {
        //处理AspectFunOperation
        AspectOperationOfPortIn.Handle[] handles = funPort.getHandles();
        if (handles != null)
        {
            if (checkSupport && !(state == State.BeforeInvoke || state == State.AfterInvoke || state == State.OnFinal))
            {
                return null;
            }
            switch (state)
            {
                case BeforeInvokeOfMethodCheck:
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        handle.beforeInvokeOfMethodCheck(oftenObject, funPort);
                    }
                    break;
                case BeforeInvoke:
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        if (!checkSupport || checkSupport && handle.supportInvokeByHandleArgs())
                        {
                            handle.beforeInvoke(oftenObject, funPort);
                        }
                    }
                    break;
                case Invoke:
                {
                    boolean hasInvoked = false;
                    for (AspectOperationOfPortIn.Handle handle : handles)
                    {
                        if (handle.needInvoke(oftenObject, funPort, returnObject))
                        {
                            hasInvoked = true;
                            returnObject = handle.invoke(oftenObject, funPort, returnObject);
                        }
                    }
                    if (!hasInvoked)
                    {
                        returnObject = funPort.invokeByHandleArgsWithoutAspect(oftenObject);
                    }
                    return returnObject;
                }

                case AfterInvoke:
                    for (int i = handles.length - 1; i >= 0; i--)
                    {
                        if (!checkSupport || checkSupport && handles[i].supportInvokeByHandleArgs())
                        {
                            handles[i].afterInvoke(oftenObject, funPort, returnObject);
                        }
                    }
                    break;

                case OnFinal:
                    for (int i = handles.length - 1; i >= 0; i--)
                    {
                        if (!checkSupport || checkSupport && handles[i].supportInvokeByHandleArgs())
                        {
                            handles[i].onFinal(oftenObject, funPort, returnObject, failedObject);
                        }
                    }
                    break;
            }

        }
        return null;
    }
}
