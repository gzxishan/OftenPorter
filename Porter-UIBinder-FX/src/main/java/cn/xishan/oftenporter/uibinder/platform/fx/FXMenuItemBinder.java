package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.Binder;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class FXMenuItemBinder<T extends MenuItem> extends Binder<T>
{
    public FXMenuItemBinder(T view)
    {
        super(view);
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_ENABLE == attrEnum)
        {
            Boolean enable = (Boolean) value;
            view.setDisable(!enable);
        } else if (AttrEnum.ATTR_VISIBLE == attrEnum)
        {
            Boolean visible = (Boolean) value;
            view.setVisible(visible);
        }
    }

    @Override
    public Object get(AttrEnum attrEnum)
    {
        Object v = null;
        if (AttrEnum.ATTR_ENABLE == attrEnum)
        {
            v = !view.isDisable();
        } else if (AttrEnum.ATTR_VISIBLE == attrEnum)
        {
            v = view.isVisible();
        }
        return v;
    }

    @Override
    public void release()
    {

    }
}
