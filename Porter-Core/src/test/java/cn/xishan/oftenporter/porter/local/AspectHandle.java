package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/11/26.
 */
public class AspectHandle extends AutoSetObjForAspectOfNormal.PatternHandle
{


    /**
     * @param methodPattern 方法模式，支持通配符:?与*。见{@linkplain Method#toString()}。
     */
    public AspectHandle(String methodPattern)
    {
        super(methodPattern);
    }

    @Override
    public boolean preInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, boolean hasInvoked,
            Object lastReturn) throws Exception
    {
        if (originMethod.getName().equals("say"))
        {
            oftenObject.putRequestData("aspect-handle", AspectHandle.class);
        }
        return false;
    }

    @Override
    public Object doInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn) throws Throwable
    {
        return lastReturn;
    }

    @Override
    public Object afterInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn) throws Exception
    {
        return lastReturn;
    }

    @Override
    public void onException(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Throwable throwable) throws Throwable
    {

    }

    @Override
    public boolean init(IConfigData configData, Object originObject, Class originClass,
            Method originMethod) throws Exception
    {
        System.out.println(originMethod);
        return true;
    }
}
