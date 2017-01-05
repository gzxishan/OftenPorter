package cn.xishan.oftenporter.demo.bridge.http.bridge.http.test;

import cn.xishan.oftenporter.bridge.http.HMain;
import cn.xishan.oftenporter.bridge.http.HttpUtil;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cheyg on 2017/1/5.
 */
public class Main {
    public static void main(String[] args) {
        HMain hMain = new HMain(true, new PName("HMain"), "utf-8", HttpUtil.getClient(null),
                                "http://127.0.0.1:8080/Demo/RemoteBridge/");

        hMain.getPLinker().currentBridge()
             .request(new PRequest(PortMethod.GET, ":Servlet1/T1/Remote/hello"), lResponse -> {
                 println(lResponse.getResponse());
             });
        LocalMain localMain = new LocalMain(true, new PName("Local"), "utf-8");
        localMain.getPLinker().link(hMain.getPLinker(), PLinker.Direction.ToItAll);

        //localMain.getPLinker().setForAnyOtherPName(hMain.getPLinker());//无法匹配的PName全部访问远程的。

        localMain.getPLinker().toAllBridge().request(new PRequest(PortMethod.GET, ":Servlet1/T1/Remote/hello"),
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
