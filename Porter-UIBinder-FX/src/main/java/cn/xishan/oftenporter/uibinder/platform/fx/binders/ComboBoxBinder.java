package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ComboBoxBinder extends ComboBoxBaseBinder
{
    public ComboBoxBinder(ComboBoxBase view)
    {
        super(view);
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (attrEnum == AttrEnum.ATTR_OTHER)
        {
            if (value instanceof ObservableList)
            {
                ObservableList observableList = (ObservableList) value;
                ComboBox comboBox = (ComboBox) view;
                comboBox.setItems(observableList);
            }
        } else
        {
            super.set(attrEnum, value);
        }
    }
}
