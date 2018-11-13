package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class DefaultBridgeLinkerTest
{
    @Test
    public void test()
    {
        testLink(BridgeLinker.Direction.ToIt);
        testLink(BridgeLinker.Direction.BothAll);
    }

    private void testLink(BridgeLinker.Direction direction)
    {
        int count = 10;
        int threads = 10;
        int n = 10;
        BridgeLinker[] bridgeLinkers = new BridgeLinker[count];
        for (int i = 0; i < bridgeLinkers.length; i++)
        {
            char c = (char) ('A' + i);
            bridgeLinkers[i] = new DefaultBridgeLinker(new BridgeName(c + ""), null,null);
        }
        //exe(threads, n, bridgeLinkers);


        bridgeLinkers[0].link(bridgeLinkers[1], direction);
        bridgeLinkers[0].link(bridgeLinkers[4], direction);
        bridgeLinkers[0].link(bridgeLinkers[5], direction);

        bridgeLinkers[1].link(bridgeLinkers[2], direction);
        bridgeLinkers[4].link(bridgeLinkers[3], direction);
        bridgeLinkers[2].link(bridgeLinkers[5], direction);

        bridgeLinkers[5].link(bridgeLinkers[4], direction);
        bridgeLinkers[5].link(bridgeLinkers[0], direction);
        bridgeLinkers[3].link(bridgeLinkers[2], direction);

        for (int i = 0; i < bridgeLinkers.length; i++)
        {
            System.out.println("***********************************");
            System.out.println(bridgeLinkers[i]);
        }
    }

    private void exe(int threads, int n, final BridgeLinker[] bridgeLinkers)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(threads, new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        final Random rand = new Random();
        final int enumLen = BridgeLinker.Direction.values().length;
        for (int i = 0; i < n; i++)
        {
            executorService.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    BridgeLinker init1 = bridgeLinkers[rand.nextInt(bridgeLinkers.length)];
                    BridgeLinker init2 = bridgeLinkers[rand.nextInt(bridgeLinkers.length)];
                    init1.link(init2, BridgeLinker.Direction.values()[rand.nextInt(enumLen)]);
                }

            });
        }

        try
        {
            executorService.shutdown();
            while (!executorService.isTerminated())
            {
                Thread.sleep(20);
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
