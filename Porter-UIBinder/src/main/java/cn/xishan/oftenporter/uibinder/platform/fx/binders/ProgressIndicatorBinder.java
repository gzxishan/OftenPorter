package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ProgressIndicatorBinder extends FXBinder<ProgressIndicator>
{
    private ChangeListener<Number> changeListener;

    public ProgressIndicatorBinder(ProgressIndicator view)
    {
        super(view);
        changeListener = new ChangeListener<Number>()
        {
            @Override
            public synchronized void changed(ObservableValue<? extends Number> observable, Number oldValue,
                    Number newValue)
            {
                doOnchange(oldValue,newValue);
            }
        };
        view.progressProperty().addListener(changeListener);
    }

    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            Double progress = (Double) value;
            view.setProgress(progress);
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
            return view.getProgress();
        } else
        {
            return super.get(attrEnum);
        }
    }

    @Override
    public void release()
    {
        super.release();
        view.progressProperty().removeListener(changeListener);
    }
}
