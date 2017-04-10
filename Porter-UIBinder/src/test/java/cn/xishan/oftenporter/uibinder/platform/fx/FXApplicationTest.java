package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import cn.xishan.oftenporter.uibinder.core.*;
import cn.xishan.oftenporter.uibinder.core.ui.OnValueChangedListener;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.List;

/**
 * FXApplication Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>10-6, 2016</pre>
 */
public class FXApplicationTest
{

    public static class Main extends Application
    {

        public static void main(String... args)
        {
            launch(args);
        }

        private UIBinderManager uiBinderManager;
        @Override
        public void init() throws Exception
        {
            super.init();
            UIBinderManager binderManager = new UIBinderManager(FXApplication.getUIPlatform(),
                    new LocalMain(true, new PName("FX0"), "utf-8"));
            PorterConf conf = binderManager.getCommonMain().newPorterConf();
            conf.getSeekPackages().addClassPorter(HelloPorter.class);
            conf.setContextName("C0");
            binderManager.getCommonMain().startOne(conf);
            this.uiBinderManager=binderManager;
            binderManager.setErrListener(new ErrListener()
            {
                @Override
                public BinderData onErr(JResponse jResponse, String pathPrefix, String tiedFun)
                {
                    LogUtil.printErrPosLn(jResponse);
                    return null;
                }

                @Override
                public void onException(Throwable throwable, String pathPrefix)
                {

                }
            });
        }

        @Override
        public void start(Stage primaryStage) throws Exception
        {

            Parent root = FXMLLoader.load(getClass().getResource("/fx/test01.fxml"));
            Scene scene = new Scene(root, 700, 500);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setScene(scene);
            primaryStage.show();


            Prefix prefix = Prefix.buildPrefix("C0", HelloPorter.class,false);
            UIProvider uiProvider = new FXUIProvider(prefix,
                    root);
            uiBinderManager.bind(null,uiProvider);
            BinderData binderData = new BinderData();
            List<BinderSet> list = binderData
                    .addSetTask(new BinderSet("ok", "name", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                            new OnValueChangedListener()
                            {
                                @Override
                                public void onChanged(String pathPrefix, String funTiedName, String varName,
                                        Object oldValue,Object newValue)
                                {
                                    LogUtil.printErrPosLn(newValue);
                                }
                            }));
            list.add(new BinderSet("ok", "checkbox", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));

            list.add(new BinderSet("ok", "text", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));
            list.add(new BinderSet("ok", "toggle", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));
            list.add(new BinderSet("ok", "radio", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));
            list.add(new BinderSet("ok", "progress", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));
            list.add(new BinderSet("ok", "slider", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));
            list.add(new BinderSet("ok", "choice", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));

            list.add(new BinderSet("ok", "choice", AttrEnum.ATTR_OTHER, FXCollections.observableArrayList("apple","orange")));
            list.add(new BinderSet("ok", "combox", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue);
                        }
                    }));

            list.add(new BinderSet("ok", "combox", AttrEnum.ATTR_OTHER, FXCollections.observableArrayList("c-apple","c-orange")));

            list.add(new BinderSet("ok", "color", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue+":"+newValue.getClass());
                        }
                    }));
            list.add(new BinderSet("ok", "date", AttrEnum.ATTR_VALUE_CHANGE_LISTENER,
                    new OnValueChangedListener()
                    {
                        @Override
                        public void onChanged(String pathPrefix, String funTiedName, String varName, Object oldValue,Object newValue)
                        {
                            LogUtil.printErrPosLn(newValue+":"+newValue.getClass());
                        }
                    }));
            list.add(new BinderSet("temp", "nameLabel", AttrEnum.ATTR_VALUE, "\u540d\u79f0:"));
            uiBinderManager.sendBinderData("/C0/Hello/", binderData);
        }

        @Override
        public void stop() throws Exception
        {
            super.stop();
            uiBinderManager.clear();
        }
    }

    @Before
    public void before() throws Exception
    {
    }

    @After
    public void after() throws Exception
    {
    }

    //@Test
    public void main()
    {
        PropertyConfigurator.configure(getClass().getResourceAsStream("/log4j.properties"));
        Main.main();
    }

}
