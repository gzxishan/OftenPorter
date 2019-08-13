package cn.xishan.oftenporter.porter.core.util;

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
    public void test1()
    {
        ConcurrentKeyLock keyLock = new ConcurrentKeyLock();
        String lockKey = "test1";

        try
        {
            keyLock.lock(lockKey);

            try
            {
                keyLock.lock(lockKey);


            } finally
            {
                keyLock.unlock(lockKey);
            }


        } finally
        {
            keyLock.unlock(lockKey);
        }

    }
}
