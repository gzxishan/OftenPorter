package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2019-08-13.
 */
public class ConcurrentKeyLockTest
{
    /**
     * 锁的可重入测试
     */
    @Test
    public void test1() throws InterruptedException
    {
        ConcurrentKeyLock keyLock = new ConcurrentKeyLock();
        String lockKey = "test1";
        int[] rs = {0};
        Thread thread = new Thread(() -> {
            try
            {
                try
                {
                    keyLock.lock(lockKey);
                    rs[0] = 1;
                    try
                    {
                        keyLock.lock(lockKey);
                        rs[0] = 2;
                    } finally
                    {
                        keyLock.unlock(lockKey);
                    }
                    rs[0] = 3;
                } finally
                {
                    keyLock.unlock(lockKey);
                }
                rs[0] = 4;
            } catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
        Thread.sleep(2000);
        Assert.assertEquals(4,rs[0]);

    }
}
