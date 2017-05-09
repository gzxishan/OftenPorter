package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.base.PortUtil;

/**
 * Created by chenyg on 2017-05-09.
 */
public class PorterParamGetterImpl implements PorterParamGetter
{

    private String context, classTied, funTied;

    public void setContext(String context)
    {
        PortUtil.checkName(context);
        this.context = context;
    }

    public void setClassTied(String classTied)
    {
        PortUtil.checkName(classTied);
        this.classTied = classTied;
    }

    public void setFunTied(String funTied)
    {
        PortUtil.checkName(funTied);
        this.funTied = funTied;
    }

    @Override
    public String getContext()
    {
        return context;
    }

    @Override
    public String getClassTied()
    {
        return classTied;
    }

    @Override
    public String getFunTied()
    {
        return funTied;
    }
}
