package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ToggleButtonBinder extends FXBinder<ToggleButton>
{
    private ChangeListener<Boolean> changeListener;

    public ToggleButtonBinder(ToggleButton view)
    {
        super(view);
        changeListener = new ChangeListener<Boolean>()
        {
            @Override
            public synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                    Boolean newValue)
            {
                doOnchange(oldValue,newValue);
            }
        };
        view.selectedProperty().addListener(changeListener);
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            Boolean select = (Boolean) value;
            view.setSelected(select);
        } else if (AttrEnum.ATTR_VALUE_CHANGE_LISTENER == attrEnum)
        {
            OnValueChangedListener
                    onValueChangedListener =
                    (OnValueChangedListener) value;
            super.onValueChangedListener = onValueChangedListener;
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
            return view.isSelected();
        } else
        {
            return super.get(attrEnum);
        }
    }

    @Override
    public void release()
    {
        super.release();
        view.selectedProperty().removeListener(changeListener);
    }
}
