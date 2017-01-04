package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;


/**
 * HMain Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>10-7, 2016</pre>
 */
public class HMainTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void test() {
        HMain hMain = new HMain(true, new PName("HMain"), "utf-8", HttpUtil.getClient(null),
                                "http://127.0.0.1:8080/Porter-Demo/RemoteBridge/C/HServer/");


        hMain.getPLinker().currentBridge()
             .request(new PRequest(PortMethod.GET, ":Servlet1/T1/Remote/hello"), lResponse -> LogUtil.printErrPosLn(lResponse.getResponse()));
        LocalMain localMain = new LocalMain(true, new PName("Local"), "utf-8");
        localMain.getPLinker().link(hMain.getPLinker(), PLinker.Direction.ToItAll);
        localMain.getPLinker().toAllBridge().request(new PRequest(PortMethod.GET, ":HMain/=Servlet1/T1/Remote/hello"),
                                                     lResponse -> LogUtil.printPosLn(lResponse.getResponse()));
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

} 
