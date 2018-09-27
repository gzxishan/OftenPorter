package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.KeyLock;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.ConcurrentKeyLock;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/14.
 */
public class KeyLockHandle extends AspectOperationOfPortIn.HandleAdapter<KeyLock>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyLockHandle.class);

    private static ConcurrentKeyLock<String> staticKeyLock;
    private Map<String, ConcurrentKeyLock<String>> keyLockMap = new HashMap<>();

    private String lockPrefix = null;
    private String[] locks, neceLocks, uneceLocks;
    private KeyLock.LockType[] lockTypes;
    private boolean combine;
    private String valueForUneceEmpty = null;

    private ConcurrentKeyLock<String> concurrentKeyLock;
    private final String ATTR_KEY = KeyUtil.random48Key();


    @Override
    public boolean init(KeyLock current, IConfigData configData, Porter porter)
    {
        return _init(current, porter, null);
    }

    private boolean _init(KeyLock keyLock, Porter porter, PorterOfFun porterOfFun)
    {
        synchronized (KeyLockHandle.class)
        {
            if (WPTool.notNullAndEmpty(keyLock.valueForUneceEmpty()))
            {
                valueForUneceEmpty = keyLock.valueForUneceEmpty();
            }
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
            if (WPTool.existsNotEmpty(this.locks,this.neceLocks,this.uneceLocks))
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
                this.combine = keyLock.combining();
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
    public boolean init(KeyLock current, IConfigData configData, PorterOfFun porterOfFun)
    {
        return _init(current, porterOfFun.getPorter(), porterOfFun);
    }


    @Override
    public void beforeInvokeOfMethodCheck(WObject wObject, PorterOfFun porterOfFun)
    {
        List<String> keys = new ArrayList<>();

        for (KeyLock.LockType type : lockTypes)
        {
            switch (type)
            {
                case LOCKS:
                    for (String key : locks)
                    {
                        if(!keys.contains(key)){
                            keys.add(lockPrefix == null ? key : lockPrefix + key);
                        }
                    }
                    break;
                case NECE_LOCKS:
                    for (String neceName : neceLocks)
                    {
                        String key = wObject.nece(neceName);
                        if(!keys.contains(key)){
                            keys.add(lockPrefix == null ? key : lockPrefix + key);
                        }
                    }
                    break;
                case UNECE_LOCKS:
                    for (String uneceName : uneceLocks)
                    {
                        String key = wObject.unece(uneceName);
                        if (valueForUneceEmpty != null && WPTool.isEmpty(key))
                        {
                            key = valueForUneceEmpty;
                        }
                        if (WPTool.notNullAndEmpty(key))
                        {
                            if(!keys.contains(key)){
                                keys.add(lockPrefix == null ? key : lockPrefix + key);
                            }
                        }
                    }
                    break;
            }
        }

        String[] locks = keys.toArray(new String[0]);
        if (combine)
        {
            locks = new String[]{
                    WPTool.join(":", locks)
            };
        }
        LOGGER.debug("locking[{}]:{}", wObject.url(), locks);
        concurrentKeyLock.locks(locks);
        LOGGER.debug("locked[{}]:{}", wObject.url(), locks);
        wObject.putRequestData(ATTR_KEY, locks);
    }

    @Override
    public boolean needInvoke(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn)
    {
        return false;
    }

    @Override
    public void onFinal(WObject wObject, PorterOfFun porterOfFun, Object lastReturn, Object failedObject)
    {
        String[] locks = wObject.removeRequestData(ATTR_KEY);
        if (locks == null)
        {
            LOGGER.warn("locks key is null from requestData:attr key={}", ATTR_KEY);
        } else
        {
            LOGGER.debug("unlocking[{}]:{}", wObject.url(), locks);
            concurrentKeyLock.unlocks(locks);
            LOGGER.debug("unlocked[{}]:{}", wObject.url(), locks);
        }
    }

}
