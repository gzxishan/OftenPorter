package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _AutoSet
{
    String value = "";
    Class<?> classValue = AutoSet.class;
    AutoSet.Range range = AutoSet.Range.Context;
    boolean nullAble = false;

    Class<? extends AutoSetDealt> dealt = AutoSetDealt.class;
    Class<? extends AutoSetGen> gen = AutoSetGen.class;
    boolean notNullPut = true;
    String option = "";

    /**
     * 是否会进行递归设置
     */
    boolean willRecursive=true;

    private boolean willSave=true;

    public _AutoSet()
    {
    }

    /**
     * 是否会保存实例的总开关，默认为true。
     * @return
     */
    public boolean isWillSave()
    {
        return willSave;
    }

    public void setWillSave(boolean willSave)
    {
        this.willSave = willSave;
    }

    public boolean willRecursive()
    {
        return willRecursive;
    }

    public String option()
    {
        return option;
    }

    public boolean notNullPut()
    {
        return notNullPut;
    }

    public boolean nullAble()
    {
        return nullAble;
    }

    public Class<? extends AutoSetDealt> dealt()
    {
        return dealt;
    }

    public Class<? extends AutoSetGen> gen()
    {
        return gen;
    }

    public String value()
    {
        return value;
    }

    public Class<?> classValue()
    {
        return classValue;
    }

    public AutoSet.Range range()
    {
        return range;
    }
}
