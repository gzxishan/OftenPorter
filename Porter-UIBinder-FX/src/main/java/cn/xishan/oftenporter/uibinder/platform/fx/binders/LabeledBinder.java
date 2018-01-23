package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.scene.control.Labeled;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class LabeledBinder extends FXBinder<Labeled>
{
    public LabeledBinder(Labeled view)
    {
        super(view);
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            view.setText(value == null ? "" : value + "");
        } else
        {
            super.set(attrEnum, value);
        }
    }

    @Override
    public Object get(AttrEnum attrEnum)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            return view.getText();
        } else
        {
            return super.get(attrEnum);
        }
    }
}
