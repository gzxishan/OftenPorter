package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
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

    public HMain(boolean responseWhenException, PName pName,
            final String urlEncoding, final OkHttpClient okHttpClient, final String hostUrlPrefix)
    {
        super();
        newLocalMain(responseWhenException, pName, urlEncoding, (request, callback) -> {
            try
            {
                String path = request.getPath();
                if (path.startsWith("/="))
                {
                    path = ":" + path.substring(2);
                }
                HttpUtil.requestWPorter(request.getParameterMap(), HttpMethod.valueOf(request.getMethod().name()),
                        okHttpClient, hostUrlPrefix + path,
                                        jResponse -> {
                                            if (callback != null)
                                            {
                                                callback.onResponse(new PResponseImpl(jResponse));
                                            }
                                        });
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
                if (callback != null)
                {
                    callback.onResponse(PResponseImpl.exception(ResultCode.EXCEPTION, e));
                }
            }
        });
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
    public void startOne(PorterConf porterConf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PLinker getPLinker()
    {
        return super.getPLinker();
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

    }

    @Override
    public PorterConf newPorterConf()
    {
        throw new UnsupportedOperationException();
    }
}
