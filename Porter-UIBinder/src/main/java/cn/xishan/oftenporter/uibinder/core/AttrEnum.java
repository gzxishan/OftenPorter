package cn.xishan.oftenporter.uibinder.core;

/**
 * 与控件属性有关的
 * Created by ZhuiFeng on 2015/6/11.
 */
public enum AttrEnum
{
    /**
     * enable控件,传入boolean
     */
    ATTR_ENABLE,
    /**
     * 与焦点有关的
     */
    ATTR_FOCUS_REQUEST,
    /**
     * 可见性
     */
    ATTR_VISIBLE,
    /**
     * 控件值
     */
    ATTR_VALUE,

    /**
     * 其他类型的设定
     */
    ATTR_OTHER,

    /**
     * 内容改变监听器
     */
    ATTR_VALUE_CHANGE_LISTENER,

    /**
     * 异步设置:则{@linkplain com.chenyg.uibinder.BinderData.Task#data}为{@linkplain com.chenyg.uibinder.AsynSetListener.Receiver}
     */
    METHOD_ASYNC_SET,
    /**
     * 用于得到值
     */
    METHOD_ASYNC_GET
}
