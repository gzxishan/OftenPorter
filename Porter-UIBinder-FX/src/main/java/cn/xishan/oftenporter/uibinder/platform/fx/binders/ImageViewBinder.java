package cn.xishan.oftenporter.uibinder.platform.fx.binders;

import cn.xishan.oftenporter.uibinder.core.AttrEnum;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import cn.xishan.oftenporter.uibinder.platform.fx.FXBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URI;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class ImageViewBinder extends FXBinder<ImageView>
{
    private EventHandler<ActionEvent> actionHandler;
    private ChangeListener<Image> changeListener;

    public ImageViewBinder(ImageView view)
    {
        super(view);
        actionHandler = new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                onOccur();
            }
        };
        view.addEventFilter(ActionEvent.ACTION, actionHandler);
        changeListener = new ChangeListener<Image>()
        {
            @Override
            public synchronized void changed(ObservableValue<? extends Image> observable, Image oldValue,
                    Image newValue)
            {
                doOnchange(oldValue, newValue);
            }
        };
        view.imageProperty().addListener(changeListener);
    }


    @Override
    public void set(AttrEnum attrEnum, Object value)
    {
        if (AttrEnum.ATTR_VALUE == attrEnum)
        {
            Image image;
            if (value == null)
            {
                image = null;
            } else if (value instanceof String)
            {
                File file = new File((String) value);
                image = new Image(file.toURI().toString());
            } else if (value instanceof File)
            {
                File file = (File) value;
                image = new Image(file.toURI().toString());
            } else if (value instanceof URI)
            {
                URI uri = (URI) value;
                image = new Image(uri.toString());
            } else
            {
                image = (Image) value;
            }
            view.setImage(image);
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
            return view.getImage();
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
        view.imageProperty().removeListener(changeListener);
    }
}
