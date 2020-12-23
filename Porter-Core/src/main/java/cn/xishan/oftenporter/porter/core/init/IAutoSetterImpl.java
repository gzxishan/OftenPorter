package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ContextPorter;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.AutoSetException;
import cn.xishan.oftenporter.porter.core.sysset.IAutoSetter;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-05-14.
 */
class IAutoSetterImpl implements IAutoSetter, IOtherStartDestroy
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IAutoSetterImpl.class);

    private AutoSetHandle autoSetHandle;
    private Set<ContextPorter.OtherStartDestroy> otherDestroyList = new HashSet<>();
    private Set<ContextPorter.OtherStartDestroy> otherStartList = new HashSet<>();

    private IOtherStartDestroy defaultIOtherStartDestroy;
    private OftenObject oftenObject;


    public IAutoSetterImpl(AutoSetHandle autoSetHandle)
    {
        this.autoSetHandle = autoSetHandle;
        autoSetHandle.setAutoSetter(this);
    }

    @Override
    public void forInstance(Object[] objects) throws AutoSetException
    {
        autoSetHandle.addAutoSetsForNotPorter(objects);
        autoSetHandle.doAutoSetNormal();
        autoSetHandle.invokeSetOk(oftenObject);
        invokeStart();
    }

    @Override
    public Object forInstanceMayProxy(Object object) throws AutoSetException
    {
        Object result = autoSetHandle.doAutoSetMayProxy(object);
        autoSetHandle.invokeSetOk(oftenObject);
        invokeStart();
        return result;
    }

    @Override
    public void forClass(Class[] classes) throws AutoSetException
    {
        autoSetHandle.addStaticAutoSet(null, null, Arrays.asList(classes),
                Thread.currentThread().getContextClassLoader());
        autoSetHandle.doAutoSetNormal();
        autoSetHandle.invokeSetOk(oftenObject);
        invokeStart();
    }

    @Override
    public void forPackage(String[] packages) throws AutoSetException
    {
        autoSetHandle.addStaticAutoSet(Arrays.asList(packages), null, null,
                Thread.currentThread().getContextClassLoader());
        autoSetHandle.doAutoSetNormal();
        autoSetHandle.invokeSetOk(oftenObject);
        invokeStart();
    }

    @Override
    public void addOtherStarts(Object object, Method[] starts)
    {
        for (Method method : starts)
        {
            if(AnnotationDealt.isNullInstance(method,object)){
                continue;
            }

            PortStart portStart = AnnoUtil.getAnnotation(method, PortStart.class);
            otherStartList.add(new ContextPorter.OtherStartDestroy(object, method, portStart.order()));
        }
    }

    @Override
    public void addOtherDestroys(Object object, Method[] destroys)
    {
        for (Method method : destroys)
        {
            if(AnnotationDealt.isNullInstance(method,object)){
                continue;
            }

            PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
            otherDestroyList.add(new ContextPorter.OtherStartDestroy(object, method, portDestroy.order()));
        }
    }

    private void invokeStart()
    {
        IConfigData configData = autoSetHandle.getConfigData();
        ContextPorter.OtherStartDestroy[] starts = otherStartList.toArray(new ContextPorter.OtherStartDestroy[0]);
        Arrays.sort(starts);
        otherStartList.clear();
        for (ContextPorter.OtherStartDestroy otherStartDestroy : starts)
        {
            try
            {
                DefaultArgumentsFactory.invokeWithArgs(configData, otherStartDestroy.object,
                        otherStartDestroy.method, oftenObject, configData);
            } catch (Exception e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    @Override
    public void onOtherDestroy()
    {
        ContextPorter.onOtherDestroy(autoSetHandle.getConfigData(),
                otherDestroyList.toArray(new ContextPorter.OtherStartDestroy[0]));
        defaultIOtherStartDestroy.onOtherDestroy();
    }

    @Override
    public boolean hasOtherStart()
    {
        return true;
    }


    void setCurrentOftenObject(OftenObject oftenObject)
    {
        this.oftenObject = oftenObject;
    }

    void onOk(OftenObject oftenObject)
    {
        this.oftenObject = oftenObject;
        defaultIOtherStartDestroy = autoSetHandle.getOtherStartDestroy();
        autoSetHandle.setIOtherStartDestroy(this);
    }
}
