package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.PorterAttr;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class HMain extends LocalMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HMain.class);
    private BridgeLinker bridgeLinker;

    private static final String HEADER_KEY = OftenKeyUtil.random48Key();
    private OkHttpClient okHttpClient;

    public HMain(boolean responseWhenException, BridgeName bridgeName,
            final String urlEncoding, OkHttpClient httpClient, final String hostUrlPrefix)
    {
        super();
        this.okHttpClient = httpClient;
        newLocalMain(responseWhenException, bridgeName, urlEncoding, (request, callback) ->
        {
            try
            {
                String path = request.getPath();
                if (path.startsWith(":"))
                {
                    path = (hostUrlPrefix.endsWith("/") ? "=" : "/=") + path.substring(1);
                } else if (path.startsWith("/") && hostUrlPrefix.endsWith("/"))
                {
                    path = path.substring(1);
                }
//                else if(path.startsWith("/:")){
//                    path = (hostUrlPrefix.endsWith("/") ? "=" : "/=") + path.substring(2);
//                }
                String url = hostUrlPrefix + path;

                Map<String, Object> params = request.getParameterMap();
                RequestData requestData = new RequestData(params);
                if (params != null)
                {
                    Map<String, String> headers = (Map<String, String>) params.remove(HEADER_KEY);
                    requestData.setHeaders(headers);
                }

                HttpUtil.requestWPorter(requestData, PortMethod.valueOf(request.getMethod().name()),
                        okHttpClient, url,
                        jResponse ->
                        {
                            if (callback != null)
                            {
                                callback.onResponse(new BridgeResponseImpl(jResponse.isSuccess(), jResponse));
                            }
                        });
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
                if (callback != null)
                {
                    callback.onResponse(BridgeResponseImpl.exception(ResultCode.EXCEPTION, e));
                }
            }
        });
        bridgeLinker = new BridgeLinker()
        {
            @Override
            public LinkListener sendLink()
            {
                return HMain.super.getBridgeLinker().sendLink();
            }

            @Override
            public void receiveLink(BridgeLinker init, LinkListener linkListener)
            {
                HMain.super.getBridgeLinker().receiveLink(init, linkListener);
            }

            @Override
            public BridgeLinker getLinkedPLinker(String pName)
            {
                return HMain.super.getBridgeLinker().getLinkedPLinker(pName);
            }

            @Override
            public void setPorterAttr(PorterAttr porterAttr)
            {
                HMain.super.getBridgeLinker().setPorterAttr(porterAttr);
            }

            @Override
            public PorterAttr getPorterAttr()
            {
                return HMain.super.getBridgeLinker().getPorterAttr();
            }

            @Override
            public void link(BridgeLinker it, Direction direction)
            {
                HMain.super.getBridgeLinker().link(it, direction);
            }

            @Override
            public void close()
            {
                HMain.super.getBridgeLinker().close();
            }

            @Override
            public boolean isClosed()
            {
                return false;
            }

            @Override
            public void setForAnyOtherPName(BridgeLinker anyOther)
            {
                HMain.super.getBridgeLinker().setForAnyOtherPName(anyOther);
            }

            @Override
            public IBridge innerBridge()
            {
                return HMain.super.getBridgeLinker().innerBridge();
            }

            @Override
            public IBridge currentBridge()
            {
                return HMain.super.getBridgeLinker().currentBridge();
            }

            @Override
            public IBridge toAllBridge()
            {
                return currentBridge();
            }

            @Override
            public BridgeName currentName()
            {
                return HMain.super.getBridgeLinker().currentName();
            }

            @Override
            public boolean isForAnyOtherPName()
            {
                return true;
            }
        };
    }

    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }

    public static String getHeaderKey()
    {
        return HEADER_KEY;
    }

    @Override
    public void addGlobalAutoSet(String name, Object object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addGlobalTypeParser(ITypeParser typeParser)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startOne(PorterConf porterConf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BridgeLinker getBridgeLinker()
    {
        return bridgeLinker;
    }

    @Override
    public void destroyOne(String contextName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableOne(String contextName, boolean enable)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroyAll()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PorterData getPorterData()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PorterConf newPorterConf(Class... importers)
    {
        throw new UnsupportedOperationException();
    }
}
