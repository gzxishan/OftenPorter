package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class DefaultPLinkerTest
{
    @Test
    public void test()
    {
        testLink(PLinker.Direction.ToIt);
        testLink(PLinker.Direction.BothAll);
    }

    private void testLink(PLinker.Direction direction)
    {
        int count = 10;
        int threads = 10;
        int n = 10;
        PLinker[] pLinkers = new PLinker[count];
        for (int i = 0; i < pLinkers.length; i++)
        {
            char c = (char) ('A' + i);
            pLinkers[i] = new DefaultPLinker(new PName(c + ""), null,null);
        }
        //exe(threads, n, pLinkers);


        pLinkers[0].link(pLinkers[1], direction);
        pLinkers[0].link(pLinkers[4], direction);
        pLinkers[0].link(pLinkers[5], direction);

        pLinkers[1].link(pLinkers[2], direction);
        pLinkers[4].link(pLinkers[3], direction);
        pLinkers[2].link(pLinkers[5], direction);

        pLinkers[5].link(pLinkers[4], direction);
        pLinkers[5].link(pLinkers[0], direction);
        pLinkers[3].link(pLinkers[2], direction);

        for (int i = 0; i < pLinkers.length; i++)
        {
            System.out.println("***********************************");
            System.out.println(pLinkers[i]);
        }
    }

    private void exe(int threads, int n, final PLinker[] pLinkers)
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
        final int enumLen = PLinker.Direction.values().length;
        for (int i = 0; i < n; i++)
        {
            executorService.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    PLinker init1 = pLinkers[rand.nextInt(pLinkers.length)];
                    PLinker init2 = pLinkers[rand.nextInt(pLinkers.length)];
                    init1.link(init2, PLinker.Direction.values()[rand.nextInt(enumLen)]);
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
