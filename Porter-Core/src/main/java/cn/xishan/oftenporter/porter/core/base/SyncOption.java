package cn.xishan.oftenporter.porter.core.base;


import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterParamGetterImpl;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.sysset.PorterSync;

/**
 * Created by chenyg on 2017-05-11.
 */
public class SyncOption
{
    private String contextName, classTied, funTied;
    private PortMethod method;

    PorterSync build(WObject wObject,boolean isInner)
    {
        SyncOption syncOption = this;
        if (syncOption.contextName == null)
        {
            syncOption.contextName = wObject.url().contextName();
        }
        if (syncOption.classTied == null)
        {
            syncOption.classTied = wObject.url().classTied();
        }
        PorterParamGetterImpl porterParamGetter = new PorterParamGetterImpl();
        porterParamGetter.setContext(syncOption.contextName);
        porterParamGetter.setClassTied(syncOption.classTied);
        porterParamGetter.setFunTied(syncOption.funTied);
        _SyncPorterOption syncPorterOption = new _SyncPorterOption(porterParamGetter);
        syncPorterOption.setMethod(syncOption.method);
        syncPorterOption.setOk();
        PorterSync porterSync = SthDeal.newSyncPorter(syncPorterOption,isInner,wObject.delivery());
        return porterSync;
    }


    public SyncOption(PortMethod method, String contextName, String classTied, String funTied)
    {
        this.contextName = contextName;
        this.classTied = classTied;
        this.funTied = funTied;
        this.method = method;
    }

    public SyncOption(PortMethod method, String classTied, String funTied)
    {
        this(method, null, classTied, funTied);
    }

    public SyncOption(PortMethod method, String funTied)
    {
        this(method, null, null, funTied);
    }

    public String getContextName()
    {
        return contextName;
    }

    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    public String getClassTied()
    {
        return classTied;
    }

    public void setClassTied(String classTied)
    {
        this.classTied = classTied;
    }

    public String getFunTied()
    {
        return funTied;
    }

    public void setFunTied(String funTied)
    {
        this.funTied = funTied;
    }

    public PortMethod getMethod()
    {
        return method;
    }

    public void setMethod(PortMethod method)
    {
        this.method = method;
    }
}
