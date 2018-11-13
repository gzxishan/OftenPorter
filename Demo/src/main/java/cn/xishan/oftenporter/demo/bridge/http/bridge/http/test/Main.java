package cn.xishan.oftenporter.demo.bridge.http.bridge.http.test;

import cn.xishan.oftenporter.bridge.http.HMain;
import cn.xishan.oftenporter.bridge.http.HttpUtil;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;

/**
 * Created by chenyg on 2017/1/5.
 */
public class Main {
    public static void main(String[] args) {
        HMain hMain = new HMain(true, new BridgeName("HMain"), "utf-8", HttpUtil.getClient(),
                                "http://127.0.0.1:8080/Demo/RemoteBridge/");

        hMain.getBridgeLinker().currentBridge()
             .request(new BridgeRequest(PortMethod.GET, ":Servlet1/T1/Remote/hello"), lResponse -> {
                 println(lResponse.getResponse());
             });
        LocalMain localMain = new LocalMain(true, new BridgeName("Local"), "utf-8");
        localMain.getBridgeLinker().link(hMain.getBridgeLinker(), BridgeLinker.Direction.ToItAll);

        //localMain.getBridgeLinker().setForAnyOtherPName(hMain.getBridgeLinker());//无法匹配的PName全部访问远程的。

        localMain.getBridgeLinker().toAllBridge().request(new BridgeRequest(PortMethod.GET, ":Servlet1/T1/Remote/hello"),
                                                     lResponse -> {
                                                         println(lResponse.getResponse());
                                                     });
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private static synchronized void println(Object object){
        LogUtil.printErrPosLnS(1,object);
    }
}
