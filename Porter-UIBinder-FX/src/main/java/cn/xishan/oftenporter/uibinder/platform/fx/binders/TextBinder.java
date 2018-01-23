package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class TextBinder extends FXBinder<TextInputControl>
{
    private ChangeListener<String> changeListener;

    public TextBinder(final TextInputControl view)
    {
        super(view);

        changeListener = new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                doOnchange(oldValue,newValue);
            }
        };
        view.textProperty().addListener(changeListener);
    }


    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            view.setText(value == null ? "" : value + "");
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
            return view.getText();
        } else
        {
            return super.get(attrEnum);
        }
    }

    @Override
    public void release()
    {
        super.release();
        view.textProperty().removeListener(changeListener);
    }
}
