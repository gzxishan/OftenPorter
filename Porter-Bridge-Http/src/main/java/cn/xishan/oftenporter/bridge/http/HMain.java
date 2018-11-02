package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.PorterAttr;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
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
    private PLinker pLinker;

    private static final String HEADER_KEY = KeyUtil.random48Key();
    private OkHttpClient okHttpClient;

    public HMain(boolean responseWhenException, PName pName,
            final String urlEncoding, OkHttpClient httpClient, final String hostUrlPrefix)
    {
        super();
        this.okHttpClient = httpClient;
        newLocalMain(responseWhenException, pName, urlEncoding, (request, callback) ->
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
                                callback.onResponse(new PResponseImpl(jResponse.isSuccess(), jResponse));
                            }
                        });
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
                if (callback != null)
                {
                    callback.onResponse(PResponseImpl.exception(ResultCode.EXCEPTION, e));
                }
            }
        });
        pLinker = new PLinker()
        {
            @Override
            public LinkListener sendLink()
            {
                return HMain.super.getPLinker().sendLink();
            }

            @Override
            public void receiveLink(PLinker init, LinkListener linkListener)
            {
                HMain.super.getPLinker().receiveLink(init, linkListener);
            }

            @Override
            public PLinker getLinkedPLinker(String pName)
            {
                return HMain.super.getPLinker().getLinkedPLinker(pName);
            }

            @Override
            public void setPorterAttr(PorterAttr porterAttr)
            {
                HMain.super.getPLinker().setPorterAttr(porterAttr);
            }

            @Override
            public PorterAttr getPorterAttr()
            {
                return HMain.super.getPLinker().getPorterAttr();
            }

            @Override
            public void link(PLinker it, Direction direction)
            {
                HMain.super.getPLinker().link(it, direction);
            }

            @Override
            public void close()
            {
                HMain.super.getPLinker().close();
            }

            @Override
            public boolean isClosed()
            {
                return false;
            }

            @Override
            public void setForAnyOtherPName(PLinker anyOther)
            {
                HMain.super.getPLinker().setForAnyOtherPName(anyOther);
            }

            @Override
            public PBridge innerBridge()
            {
                return HMain.super.getPLinker().innerBridge();
            }

            @Override
            public PBridge currentBridge()
            {
                return HMain.super.getPLinker().currentBridge();
            }

            @Override
            public PBridge toAllBridge()
            {
                return currentBridge();
            }

            @Override
            public PName currentPName()
            {
                return HMain.super.getPLinker().currentPName();
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
    public PLinker getPLinker()
    {
        return pLinker;
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
