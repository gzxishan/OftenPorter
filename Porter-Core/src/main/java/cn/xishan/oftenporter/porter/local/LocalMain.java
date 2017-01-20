package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.PreRequest;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.simple.DefaultPorterBridge;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;

/**
 * Created by https://github.com/CLovinr on 2016/9/1.
 */
public class LocalMain implements CommonMain
{
    protected PorterMain porterMain;

    public LocalMain(boolean responseWhenException, PName pName, String urlEncoding)
    {
        PBridge bridge = (request, callback) -> {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = porterMain.forRequest(request, resp);
            if (req != null)
            {
                porterMain.doRequest(req, request, resp);
            }
        };
        porterMain = new PorterMain(pName,this, bridge);
        porterMain.init(new DefaultUrlDecoder(urlEncoding), responseWhenException);
    }


    /**
     * 接着请调用{@linkplain #newLocalMain(boolean, PName, String, PBridge)}
     */
    protected LocalMain()
    {

    }

    protected void newLocalMain(boolean responseWhenException, PName pName, String urlEncoding, PBridge bridge)
    {
        porterMain = new PorterMain(pName,this, bridge);
        porterMain.init(new DefaultUrlDecoder(urlEncoding), responseWhenException);
    }

    protected UrlDecoder getUrlDecoder()
    {
        return porterMain.getUrlDecoder();
    }

    @Override
    public void addGlobalAutoSet(String name, Object object)
    {
        porterMain.addGlobalAutoSet(name, object);
    }

    @Override
    public void addGlobalTypeParser(ITypeParser typeParser)
    {
        porterMain.addGlobalTypeParser(typeParser);
    }

    @Override
    public void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException
    {
        porterMain.addGlobalCheck(checkPassable);
    }

    @Override
    public void startOne(PorterConf porterConf)
    {
        porterMain.startOne(DefaultPorterBridge.defaultBridge(porterConf));
    }

    @Override
    public PLinker getPLinker()
    {
        return porterMain.getPLinker();
    }

    @Override
    public void destroyOne(String contextName)
    {
        porterMain.destroyOne(contextName);
    }

    @Override
    public void enableOne(String contextName, boolean enable)
    {
        porterMain.enableContext(contextName, enable);
    }

    @Override
    public void destroyAll()
    {
        porterMain.destroyAll();
    }

    @Override
    public PorterConf newPorterConf()
    {
        return porterMain.newPorterConf();
    }
}
