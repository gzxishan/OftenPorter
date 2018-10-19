package cn.xishan.oftenporter.porter.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于键的锁。
 * Created by https://github.com/CLovinr on 2017/9/2.
 */
public class ConcurrentKeyLock<K> implements AutoCloseable
{

    private ThreadLocal<K[]> threadLocal = new ThreadLocal<>();

    /**
     * 将keys与ThreadLocal绑定,通过{@linkplain #close()}进行锁的释放
     *
     * @param keys
     */
    public ConcurrentKeyLock<K> lockLocalThread(K... keys)
    {
        threadLocal.set(keys);
        locks(keys);
        return this;
    }

    /**
     * 见{@linkplain #lockLocalThread(K...)}
     */
    @Override
    public void close()
    {
        K[] keys = threadLocal.get();
        if (keys != null)
        {
            unlocks(keys);
            threadLocal.remove();
        }
    }

    public interface Locker<K>
    {
        void lock(K key);

        void unlock(K key);
    }

    private static class LockInfo
    {
        private final Semaphore semaphore;
        private int lockCount;
        private String thread;

        private LockInfo()
        {
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquireUninterruptibly();
            this.semaphore = semaphore;
            this.lockCount = 1;
            if (LOGGER.isDebugEnabled())
            {
                thread = Thread.currentThread().toString();
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentKeyLock.class);
    private static final ThreadLocal<Map<Object, LockInfo>> local = ThreadLocal.withInitial(() -> new HashMap<>(5));

    private final ConcurrentMap<K, LockInfo> map = new ConcurrentHashMap<>();
    private AtomicLong acquireCount = new AtomicLong(0), releaseCount = new AtomicLong(0);
    private Locker<K> locker;


    public ConcurrentKeyLock()
    {
        locker = new Locker<K>()
        {
            @Override
            public void lock(K key)
            {
                if (key == null)
                    return;
                LockInfo info = local.get().get(key);

                if (info == null)
                {
                    LockInfo current = new LockInfo();
                    local.get().put(key, current);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("acquire count={}", acquireCount.incrementAndGet());
                    }
                    LockInfo previous = map.put(key, current);
                    if (previous != null)
                    {
                        LOGGER.debug("waiting other:key={},thread={},previous={}", key, current.thread,
                                previous.thread);
                        previous.semaphore.acquireUninterruptibly();
                        LOGGER.debug("acquired resource:key={},thread={},previous={}", key, current.thread,
                                previous.thread);
                    } else
                    {
                        LOGGER.debug("acquired resource:key={},thread={}", key, current.thread);
                    }
                } else
                {
                    info.lockCount++;
                    LOGGER.debug("acquired resource in the same thread:key={},thread={}", key, info.thread);
                }
            }

            @Override
            public void unlock(K key)
            {
                if (key == null)
                    return;
                LockInfo info = local.get().get(key);
                if (info != null && --info.lockCount <= 0)
                {
                    LOGGER.debug("unlocking:key={},thread={}", key, info.thread);
                    local.get().remove(key);
                    map.remove(key, info);
                    info.semaphore.release();
                    LOGGER.debug("unlocked:key={},thread={}", key, info.thread);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("unlock count={}", releaseCount.incrementAndGet());
                    }
                }
            }
        };
    }

    public ConcurrentKeyLock(Locker locker)
    {
        this.locker = locker;
    }

    /**
     * 锁定key，其他等待此key的线程将进入等待，直到调用{@link #unlock(K)}
     * 使用hashcode和equals来判断key是否相同，因此key必须实现{@link #hashCode()}和
     * {@link #equals(Object)}方法
     *
     * @param key
     */
    public void lock(K key)
    {
        locker.lock(key);
    }

    /**
     * 释放key，唤醒其他等待此key的线程
     *
     * @param key
     */
    public void unlock(K key)
    {
        locker.unlock(key);
    }

    /**
     * 锁定多个key
     * 建议在调用此方法前先对keys进行排序，使用相同的锁定顺序，防止死锁发生
     *
     * @param keys
     */
    public void locks(K... keys)
    {
        if (keys == null)
            return;
        for (K key : keys)
        {
            lock(key);
        }
    }

    /**
     * 释放多个key
     *
     * @param keys
     */
    public void unlocks(K... keys)
    {
        if (keys == null)
            return;
        for (K key : keys)
        {
            unlock(key);
        }
    }


}
