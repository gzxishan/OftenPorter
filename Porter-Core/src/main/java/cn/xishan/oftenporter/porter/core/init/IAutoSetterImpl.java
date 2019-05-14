package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ContextPorter;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.exception.AutoSetException;
import cn.xishan.oftenporter.porter.core.sysset.IAutoSetter;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-05-14.
 */
class IAutoSetterImpl implements IAutoSetter, IOtherStartDestroy
{
    private AutoSetHandle autoSetHandle;
    private Set<ContextPorter.OtherStartDestroy> otherDestroyList = new HashSet<>();
    private boolean isOk = false;


    public IAutoSetterImpl(AutoSetHandle autoSetHandle)
    {
        this.autoSetHandle = autoSetHandle;
        autoSetHandle.setAutoSetter(this);
    }

    @Override
    public void forInstance(Object[] objects) throws AutoSetException
    {
        checkOk();
        autoSetHandle.addAutoSetsForNotPorter(objects);
        autoSetHandle.doAutoSetNormal();
        autoSetHandle.invokeSetOk(null);
    }

    @Override
    public void forClass(Class[] classes) throws AutoSetException
    {
        checkOk();
        autoSetHandle.addStaticAutoSet(null, null, Arrays.asList(classes),
                Thread.currentThread().getContextClassLoader());
        autoSetHandle.doAutoSetNormal();
        autoSetHandle.invokeSetOk(null);
    }

    @Override
    public void addOtherStarts(Object object, Method[] starts)
    {

    }

    @Override
    public void addOtherDestroys(Object object, Method[] destroys)
    {
        for (Method method : destroys)
        {
            PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
            otherDestroyList.add(new ContextPorter.OtherStartDestroy(object, method, portDestroy.order()));
        }
    }

    @Override
    public void onOtherDestroy()
    {
        ContextPorter.onOtherDestroy(autoSetHandle.getConfigData(),
                otherDestroyList.toArray(new ContextPorter.OtherStartDestroy[0]));
    }

    @Override
    public boolean hasOtherStart()
    {
        return false;
    }

    private void checkOk()
    {
        if (!isOk)
        {
            throw new AutoSetException("not ok");
        }
    }

    void onOk()
    {
        autoSetHandle.setIOtherStartDestroy(this);
        this.isOk = true;
    }
}
