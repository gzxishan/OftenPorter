package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.PorterAttr;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认实现,记录所有可达路径。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class DefaultBridgeLinker implements BridgeLinker
{
    protected boolean isForAny = false;
    private BridgeName bridgeName;
    private IBridge current, inner;
    private LinkListener linkListener;
    private Map<BridgeName, LinkListener> listenerMap;

    //<BridgeName,BridgePath>
    private Map<String, BridgePath> pathMap;
    private static final Object LOCK = new Object();
    private IBridge toAll;
    private boolean isClosed = false;
    private PorterAttr porterAttr;
    private BridgeLinker anyOther;

    private static class Response extends BridgeResponse
    {

        protected Response(boolean isOk, Object object)
        {
            super(isOk, object);
        }
    }


    private final Logger LOGGER;

    public DefaultBridgeLinker(BridgeName bridgeName, IBridge currentBridge, IBridge innerBridge)
    {
        LOGGER = LogUtil.logger(DefaultBridgeLinker.class);
        this.bridgeName = bridgeName;
        this.current = currentBridge;
        this.inner = innerBridge;
        pathMap = new ConcurrentHashMap<>();
        listenerMap = new ConcurrentHashMap<>();

        //自己可达自己
        pathMap.put(bridgeName.getName(), new BridgePath(0, bridgeName, this));

        linkListener = (it, bridgePath) -> {
            synchronized (LOCK)
            {
                if (bridgePath.bridgeName.equals(currentName()))
                {//不用再添加自己。
                    return;
                }
                BridgePath path = pathMap.get(bridgePath.bridgeName.getName());
                if (path == null || path.step > bridgePath.step + 1)
                {//保存路径更短者
                    BridgePath newPath = bridgePath.newPath(bridgePath.step + 1);
                    putPath(newPath);
                    //通知其他人我所达到的路径
                    forAll().onItCanGo(DefaultBridgeLinker.this, newPath);
                    //接收对方的。
                    newPath.bridgeLinker.receiveLink(DefaultBridgeLinker.this, linkListener);
                }
            }
        };


        toAll = new IBridge()
        {
            BridgeUrlDecoder bridgeUrlDecoder = new DefaultBridgeUrlDecoder();

            @Override
            public void request(BridgeRequest request, BridgeCallback callback)
            {
                BridgeUrlDecoder.Result result = bridgeUrlDecoder.decode(request.getPath());
                BridgePath path = null;
                if (result == null || (path = pathMap.get(result.bridgeName())) == null || path.bridgeLinker.isClosed())
                {
                    if (anyOther != null)
                    {
                        //TODO 防止循环
                        anyOther.toAllBridge().request(request, callback);
                    } else
                    {
                        if (path != null && path.bridgeLinker.isClosed())
                        {
                            pathMap.remove(result.bridgeName());
                        }
                        JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
                        jResponse.setDescription(":" + (result == null ? "" : result.bridgeName()) + request.getPath());
                        BridgeResponse response = new Response(false, jResponse);
                        callback.onResponse(response);
                    }
                } else
                {
                    path.bridgeLinker.currentBridge().request(request.withNewPath(result.path()), callback);
                }

            }
        };

    }


    @Override
    public IBridge currentBridge()
    {
        return current;
    }

    @Override
    public IBridge innerBridge()
    {
        return inner;
    }

    @Override
    public BridgeName currentName()
    {
        return bridgeName;
    }

    @Override
    public IBridge toAllBridge()
    {
        return toAll;
    }

    @Override
    public LinkListener sendLink()
    {
        return linkListener;
    }


    @Override
    public void receiveLink(BridgeLinker it, LinkListener linkListener)
    {
        synchronized (LOCK)
        {

            if (linkListener == null)
            {
                listenerMap.remove(it.currentName());
                if (anyOther != null && anyOther.currentName().equals(it.currentName()))
                {
                    anyOther = null;
                }
            } else
            {
                listenerMap.put(it.currentName(), linkListener);

                sendCurrentPath2Listener(linkListener, true);
                if (it.isForAnyOtherPName())
                {
                    setForAnyOtherPName(it);
                }
            }
        }
    }

    @Override
    public BridgeLinker getLinkedPLinker(String pName)
    {
        BridgePath path = pathMap.get(pName);
        return path == null ? null : path.bridgeLinker;
    }

    @Override
    public void setPorterAttr(PorterAttr porterAttr)
    {
        this.porterAttr = porterAttr;
    }

    @Override
    public PorterAttr getPorterAttr()
    {
        return this.porterAttr;
    }

    //警告提示重复添加
    private void putPath(BridgePath path)
    {
        BridgePath last = pathMap.put(path.bridgeLinker.currentName().getName(), path);
        if (last != null && !last.bridgeLinker.equals(path.bridgeLinker))
        {
            LOGGER.warn("BridgeName '{}' been added before(current:{},last:{})", path.bridgeName, path.bridgeLinker,
                    last.bridgeLinker);
        }
    }

    //发送目前可达的路径
    private void sendCurrentPath2Listener(LinkListener listener, boolean sendMeMore)
    {
        synchronized (LOCK)
        {
            //自己可达自己，步数为0.
            listener.onItCanGo(this, new BridgePath(0, currentName(), this));
            if (sendMeMore)
            {
                Iterator<BridgePath> pathIterator = pathMap.values().iterator();
                while (pathIterator.hasNext())
                {
                    listener.onItCanGo(this, pathIterator.next());
                }
            }

        }
    }

    private LinkListener forAll()
    {
        synchronized (LOCK)
        {
            LinkListener all = (it, bridgePath) -> {
                Iterator<LinkListener> iterator = listenerMap.values().iterator();
                while (iterator.hasNext())
                {
                    iterator.next().onItCanGo(it, bridgePath);
                }
            };
            return all;
        }
    }

    @Override
    public void link(BridgeLinker it, BridgeLinker.Direction direction)
    {
        synchronized (LOCK)
        {
            LOGGER.debug("link [{}]:{}:[{}]", currentName(), direction, it.currentName());
            boolean sendMyGoPath = false;
            boolean sendMeMore = false;
            boolean addIt = false;
            boolean addItMore = false;
            switch (direction)
            {
                case Both:
                {
                    addIt = true;
                    sendMyGoPath = true;
                }
                break;
                case ToMe:
                {
                    sendMyGoPath = true;
                }
                break;
                case ToIt:
                {
                    addIt = true;
                }
                break;
                case BothAll:
                {
                    addIt = true;
                    sendMyGoPath = true;
                    addItMore = true;
                    sendMeMore = true;
                }
                break;
                case ToMeAll:
                {
                    sendMyGoPath = true;
                    sendMeMore = true;
                }
                break;
                case ToItAll:
                {
                    addIt = true;
                    addItMore = true;
                }
                break;
            }

            if (addIt)
            {
                BridgePath path = new BridgePath(1, it.currentName(), it);

                putPath(path);

                //发送新添加的可达路径给所有监听者。
                forAll().onItCanGo(this, path);

                if (addItMore)
                {
                    //用于接收对方可达的路径
                    it.receiveLink(this, linkListener);
                }

            }

            if (sendMyGoPath)
            {
                LinkListener listener = it.sendLink();
                sendCurrentPath2Listener(listener, sendMeMore);
            }
            if (it.isForAnyOtherPName())
            {
                setForAnyOtherPName(it);
            }
        }
    }

    @Override
    public synchronized void close()
    {
        isClosed = true;
        Iterator<BridgePath> iterator = pathMap.values().iterator();
        while (iterator.hasNext())
        {
            BridgePath bridgePath = iterator.next();
            if (!bridgePath.bridgeLinker.isClosed())
            {
                bridgePath.bridgeLinker.receiveLink(this, null);
            }
        }
        pathMap.clear();
        listenerMap.clear();
    }

    @Override
    public synchronized boolean isClosed()
    {
        return isClosed;
    }

    @Override
    public void setForAnyOtherPName(BridgeLinker anyOther)
    {
        this.anyOther = anyOther;
    }

    @Override
    public boolean isForAnyOtherPName()
    {
        return isForAny;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(currentName().toString()).append("\n");
        Iterator<BridgePath> pathIterator = pathMap.values().iterator();
        while (pathIterator.hasNext())
        {
            builder.append("\t").append(pathIterator.next()).append("\n");
        }
        return builder.toString();
    }
}
