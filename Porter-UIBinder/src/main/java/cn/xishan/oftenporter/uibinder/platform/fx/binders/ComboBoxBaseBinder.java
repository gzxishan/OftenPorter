package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBoxBase;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ComboBoxBaseBinder extends FXBinder<ComboBoxBase>
{
    private ChangeListener<Object> changeListener;

    public ComboBoxBaseBinder(ComboBoxBase view)
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
        } else
        {
            super.set(attrEnum, value);
        }
    }

    @Override
    public void release()
    {
        super.release();
        view.valueProperty().removeListener(changeListener);
    }
}
