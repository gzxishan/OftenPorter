package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.PorterAttr;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ListenerAdder;
import cn.xishan.oftenporter.porter.core.base.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.local.LocalMain;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public class HMain extends LocalMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HMain.class);
    private PLinker pLinker;

    public HMain(boolean responseWhenException, PName pName,
            final String urlEncoding, final OkHttpClient okHttpClient, final String hostUrlPrefix)
    {
        super();
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
                HttpUtil.requestWPorter(request.getParameterMap(), HttpMethod.valueOf(request.getMethod().name()),
                        okHttpClient, url,
                        jResponse ->
                        {
                            if (callback != null)
                            {
                                callback.onResponse(new PResponseImpl(jResponse));
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
    public ListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
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
    public PorterConf newPorterConf()
    {
        throw new UnsupportedOperationException();
    }
}
