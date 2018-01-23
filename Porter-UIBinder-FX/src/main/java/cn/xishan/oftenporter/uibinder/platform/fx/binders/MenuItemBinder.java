package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXMenuItemBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/24.
 */
public class MenuItemBinder extends FXMenuItemBinder<MenuItem>
{
    private EventHandler<ActionEvent> actionHandler;
    private ChangeListener<String> changeListener;

    public MenuItemBinder(MenuItem menuItem)
    {
        super(menuItem);
        actionHandler = new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                onOccur();
            }
        };
        menuItem.addEventHandler(ActionEvent.ACTION,actionHandler);
        changeListener = new ChangeListener<String>()
        {
            @Override
            public synchronized void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue)
            {
                doOnchange(oldValue,newValue);
            }
        };
        menuItem.textProperty().addListener(changeListener);
    }


    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            view.setText(value.toString());
        } else if (attrEnum == AttrEnum.ATTR_VALUE_CHANGE_LISTENER)
        {
            onValueChangedListener = (OnValueChangedListener) value;
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
        view.removeEventHandler(ActionEvent.ACTION, actionHandler);
        view.textProperty().removeListener(changeListener);
    }
}
