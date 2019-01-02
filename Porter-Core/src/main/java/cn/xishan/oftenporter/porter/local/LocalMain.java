package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.sysset.IAutoVarGetter;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.simple.DefaultPorterBridge;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;

/**
 * Created by https://github.com/CLovinr on 2016/9/1.
 */
public class LocalMain implements CommonMain
{
    protected PorterMain porterMain;

    public LocalMain(boolean responseWhenException, BridgeName bridgeName, String urlEncoding)
    {
        porterMain = new PorterMain(bridgeName, this);
        porterMain.init(new DefaultUrlDecoder(urlEncoding), responseWhenException);
    }


    /**
     * 接着请调用{@linkplain #newLocalMain(boolean, BridgeName, String, IBridge)}
     */
    protected LocalMain()
    {

    }

    protected void newLocalMain(boolean responseWhenException, BridgeName bridgeName, String urlEncoding,
            IBridge bridge)
    {
        porterMain = new PorterMain(bridgeName, this, bridge, bridge);
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
    public IListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
    {
        return porterMain.getOnPorterAddListenerAdder();
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
    public BridgeLinker getBridgeLinker()
    {
        return porterMain.getBridgeLinker();
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
    public PorterData getPorterData()
    {
        return porterMain.getPorterData();
    }

    @Override
    public String getDefaultTypeParserId()
    {
        return null;
    }

    @Override
    public IAutoVarGetter getAutoVarGetter(String context)
    {
        return porterMain.getAutoVarGetter(context);
    }

    @Override
    public PorterConf newPorterConf(Class... importers)
    {
        PorterConf porterConf = porterMain.newPorterConf();
        try
        {
            porterMain.seekImporter(porterConf, importers);
        } catch (Throwable throwable)
        {
            try
            {
                throw throwable;
            } catch (Throwable throwable1)
            {
                throwable1.printStackTrace();
            }
        }

        return porterConf;
    }
}
