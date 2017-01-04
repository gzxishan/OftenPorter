package cn.xishan.oftenporter.uibinder.core;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;

public abstract class Binder<T> implements Cloneable
{


    protected PorterOccur porterOccur;
    protected T view;
    private IdDeal.Result result;
    //    private static final Object EMPTY_OBJ = new Object();
//    protected Object currentValue = EMPTY_OBJ;
    protected OnValueChangedListener onValueChangedListener;

    public Binder(T view)
    {
        this.view = view;
    }


    protected final void set(IdDeal.Result result, PorterOccur porterOccur)
    {
        this.result = result;
        this.porterOccur = porterOccur;
    }

    protected void onInitOk(){

    }


    public IdDeal.Result getResult()
    {
        return result;
    }

    /**
     * 设置某个值
     *
     * @param attrEnum
     * @param value
     */
    public abstract void set(AttrEnum attrEnum, Object value);

    /**
     * 得到某个值
     *
     * @param attrEnum
     * @return
     */
    public abstract Object get(AttrEnum attrEnum);


    /**
     * 释放资源
     */
    public abstract void release();

    public interface PorterOccur
    {
        void doPorter(String pathPrefix, String tiedFun, PortMethod method);
    }

    protected void onOccur()
    {
        if (porterOccur != null)
        {
            String[] funs = result.getFunNames();
            for (int i = 0; i < funs.length; i++)
            {
                porterOccur.doPorter(result.getPathPrefix(), funs[i], result.getMethod());
            }
        }
    }

    protected synchronized void doOnchange(Object oldValue, Object newValue)
    {
        String[] funs = result.getFunNames();
        doOnchange(result.getPathPrefix(), funs[0], result.getVarName(), oldValue, newValue);
    }

    private void doOnchange(String prefix, String tiedFun, String varName, Object oldValue, Object newValue)
    {
        if (tiedFun != null&&onValueChangedListener!=null)
        {
            onValueChangedListener
                    .onChanged(prefix, tiedFun, varName,
                            oldValue, newValue);
        }
    }

}