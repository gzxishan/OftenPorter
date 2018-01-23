package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ChoiceBoxBinder extends FXBinder<ChoiceBox>
{
    private ChangeListener<Object> changeListener;

    public ChoiceBoxBinder(ChoiceBox view)
    {
        super(view);
        changeListener = new ChangeListener<Object>()
        {
            @Override
            public synchronized void changed(ObservableValue<?> observable, Object oldValue, Object newValue)
            {
                doOnchange(oldValue, newValue);
            }
        };
        view.valueProperty().addListener(changeListener);
    }

    @Override
    public Object get(AttrEnum attrEnum)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            return view.getValue();
        } else
        {
            return super.get(attrEnum);
        }
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (attrEnum == AttrEnum.ATTR_VALUE)
        {
            view.setValue(value);
        } else if (attrEnum == AttrEnum.ATTR_VALUE_CHANGE_LISTENER)
        {
            onValueChangedListener = (OnValueChangedListener) value;
        } else if (attrEnum == AttrEnum.ATTR_OTHER)
        {
            ObservableList observableList = null;
            if (value instanceof ObservableList)
            {
                observableList = (ObservableList) value;
            } else if (value instanceof String[])
            {
                observableList = FXCollections.observableArrayList((String[]) value);
            } else if (value instanceof List)
            {
                observableList = FXCollections.observableList((List) value);
            }
            view.setItems(observableList);
        } else
        {
            super.set(attrEnum, value);
        }
    }
}
