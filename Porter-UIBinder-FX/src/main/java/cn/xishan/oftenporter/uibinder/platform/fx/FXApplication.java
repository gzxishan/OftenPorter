package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.uibinder.core.BinderFactory;
import cn.xishan.oftenporter.uibinder.core.UIPlatform;
import cn.xishan.oftenporter.uibinder.platform.fx.binders.*;
import cn.xishan.oftenporter.uibinder.simple.DefaultUIPlatform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class FXApplication
{
    private static UIPlatform uiPlatform;

    static
    {
        BinderFactory binderFactory = new BinderFactory(MenuItem.class,Node.class);
        //MenuItem
        binderFactory.put(MenuItem.class,MenuItemBinder.class);
        //Node
        binderFactory.put(Button.class, ButtonBaseBinder.class);
        binderFactory.put(TextInputControl.class, TextBinder.class);
        binderFactory.put(Labeled.class, LabeledBinder.class);
        binderFactory.put(CheckBox.class, CheckBoxBinder.class);
        binderFactory.put(ToggleButton.class, ToggleButtonBinder.class);
        binderFactory.put(ProgressIndicator.class, ProgressIndicatorBinder.class);
        binderFactory.put(Slider.class, SliderBinder.class);
        binderFactory.put(ChoiceBox.class, ChoiceBoxBinder.class);
        binderFactory.put(ComboBoxBase.class, ComboBoxBaseBinder.class);
        binderFactory.put(ComboBox.class, ComboBoxBinder.class);
        /////
        binderFactory.put(ImageView.class, ImageViewBinder.class);
        uiPlatform = new DefaultUIPlatform(binderFactory);
    }


    public static UIPlatform getUIPlatform()
    {
        return uiPlatform;
    }
}
