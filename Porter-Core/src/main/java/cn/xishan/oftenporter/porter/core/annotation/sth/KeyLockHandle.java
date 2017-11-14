package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.KeyLock;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.ConcurrentKeyLock;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/14.
 */
public class KeyLockHandle implements AspectFunOperation.Handle<KeyLock>
{
    private static ConcurrentKeyLock<String> staticKeyLock;
    private static Map<String, ConcurrentKeyLock<String>> keyLockMap = new HashMap<>();

    private String lockPrefix = null;
    private String[] locks, neceLocks, uneceLocks;
    private KeyLock.LockType[] lockTypes;

    private ConcurrentKeyLock<String> concurrentKeyLock;

    @Override
    public boolean init(KeyLock current, Porter porter)
    {
        return _init(current, porter, null);
    }

    private boolean _init(KeyLock keyLock, Porter porter, PorterOfFun porterOfFun)
    {
        synchronized (KeyLockHandle.class)
        {
            if (WPTool.notNullAndEmpty(keyLock.lockPrefix()))
            {
                lockPrefix = keyLock.lockPrefix();
            }
            String[] locks = keyLock.locks();
            String[] neceLocks = keyLock.neceLocks();
            String[] uneceLocks = keyLock.uneceLocks();
            KeyLock.LockType[] lockTypes = keyLock.types();
            List<KeyLock.LockType> lockTypeList = new ArrayList<>();
            for (KeyLock.LockType type : lockTypes)
            {
                switch (type)
                {
                    case LOCKS:
                        this.locks = locks;
                        if (locks.length > 0)
                        {
                            lockTypeList.add(type);
                        }
                        break;
                    case NECE_LOCKS:
                        this.neceLocks = neceLocks;
                        if (neceLocks.length > 0)
                        {
                            lockTypeList.add(type);
                        }
                        break;
                    case UNECE_LOCKS:
                        this.uneceLocks = uneceLocks;
                        if (uneceLocks.length > 0)
                        {
                            lockTypeList.add(type);
                        }
                        break;
                }
            }
            if (this.locks != null && this.locks.length > 0 ||
                    this.neceLocks != null && this.neceLocks.length > 0 ||
                    this.uneceLocks != null && this.uneceLocks.length > 0)
            {
                switch (keyLock.range())
                {
                    case STATIC:
                        if (staticKeyLock == null)
                        {
                            staticKeyLock = new ConcurrentKeyLock<>();
                        }
                        this.concurrentKeyLock = staticKeyLock;
                        break;
                    case PNAME:
                        this.concurrentKeyLock = getKeyLock(porter.getPName().getName());
                        break;
                    case CONTEXT:
                        this.concurrentKeyLock = getKeyLock(
                                porter.getPName().getName() + "/" + porter.getContextName());
                        break;
                    case PORTER:
                        this.concurrentKeyLock = getKeyLock(
                                porter.getPName().getName() + "/" + porter.getContextName() + "/" + WPTool
                                        .join(":", porter.getPortIn().getTiedNames()));
                        break;
                    case FUN:
                        if (porterOfFun == null)
                        {
                            throw new RuntimeException(KeyLock.LockRange.FUN + " is not for class!");
                        }
                        this.concurrentKeyLock = getKeyLock(
                                porter.getPName().getName() + "/" + porter.getContextName() + "/" + WPTool
                                        .join(":", porter.getPortIn().getTiedNames()) + "/" + WPTool
                                        .join(":", porterOfFun.getMethodPortIn().getTiedNames()));
                        break;
                }
                this.lockTypes = lockTypeList.toArray(new KeyLock.LockType[0]);
                return true;
            } else
            {
                return false;
            }
        }
    }

    private ConcurrentKeyLock<String> getKeyLock(String key)
    {
        if (!keyLockMap.containsKey(key))
        {
            keyLockMap.put(key, new ConcurrentKeyLock<>());
        }
        return keyLockMap.get(key);
    }

    @Override
    public boolean init(KeyLock current, PorterOfFun porterOfFun)
    {
        return _init(current, porterOfFun.getPorter(), porterOfFun);
    }

    @Override
    public Object invokeMethod(WObject wObject, PorterOfFun fun, Object lastReturn) throws Exception
    {
        List<String> keys = new ArrayList<>();

        for (KeyLock.LockType type : lockTypes)
        {
            switch (type)
            {
                case LOCKS:
                    for (String key : locks)
                    {
                        keys.add(lockPrefix == null ? key : lockPrefix + key);
                    }
                    break;
                case NECE_LOCKS:
                    for (String neceName : neceLocks)
                    {
                        String key = wObject.nece(neceName);
                        keys.add(lockPrefix == null ? key : lockPrefix + key);
                    }
                    break;
                case UNECE_LOCKS:
                    for (String uneceName : uneceLocks)
                    {
                        String key = wObject.unece(uneceName);
                        if (WPTool.notNullAndEmpty(key))
                        {
                            keys.add(lockPrefix == null ? key : lockPrefix + key);
                        }
                    }
                    break;
            }
        }

        String[] locks = keys.toArray(new String[0]);
        try
        {
            concurrentKeyLock.lock(locks);
            return fun.invoke(wObject, null);
        } finally
        {
            concurrentKeyLock.unlock(locks);
        }
    }

    @Override
    public OutType getOutType()
    {
        return null;
    }
}
