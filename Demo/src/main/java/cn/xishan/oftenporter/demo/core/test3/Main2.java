package cn.xishan.oftenporter.demo.core.test3;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main2 {

    public static void main(String[] args) {
        /*
         * <pre>
         * 1.StateListener:用于监听框架Context的状态。
         * </pre>
         */
        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setOftenContextName("Test3-2Main");

        conf.addStateListener(new StateListener.Adapter() {
            @Override
            public void beforeSeek(InitParamSource initParamSource,
                                   PorterConf porterConf,
                                   ParamSourceHandleManager paramSourceHandleManager) {
                super.beforeSeek(initParamSource, porterConf,
                                 paramSourceHandleManager);
            }
        });

        localMain.startOne(conf);

        localMain.destroyAll();

    }

}
