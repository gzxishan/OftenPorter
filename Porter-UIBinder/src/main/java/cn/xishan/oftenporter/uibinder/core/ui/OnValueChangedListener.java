package cn.xishan.oftenporter.uibinder.core.ui;

/**
 * Created by ZhuiFeng on 2015/7/11.
 */
public interface OnValueChangedListener
{
    /**
     * @param pathPrefix  接口路径前缀
     * @param funTiedName 接口方法绑定名
     * @param varName     该控件绑定的变量名称
     * @param oldValue    旧的值
     * @param newValue    新的值
     */
    void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue, Object newValue);
}
