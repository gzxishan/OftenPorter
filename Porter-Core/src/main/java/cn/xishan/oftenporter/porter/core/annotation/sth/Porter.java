package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Porter.class);

    Object object;
    Class<?> clazz;
    _PortIn portIn;

    _PortStart[] starts;
    _PortDestroy[] destroys;
    Map<String, PorterOfFun> children;
    Porter[] mixins;

    InObj inObj;
    private AutoSetUtil autoSetUtil;

    public Porter(AutoSetUtil autoSetUtil)
    {
        this.autoSetUtil = autoSetUtil;
    }

//    public Map<String, PorterOfFun> getChildren()
//    {
//        return children;
//    }

    private _PortStart[] getStarts()
    {
        return starts;
    }

    private _PortDestroy[] getDestroys()
    {
        return destroys;
    }

    public InObj getInObj()
    {
        return inObj;
    }

    public _PortIn getPortIn()
    {
        return portIn;
    }


    void doAutoSet(Map<String, Object> autoSetMixinMap)
    {
        if (object == null)
        {
            try
            {
                object = WPTool.newObject(clazz);
            } catch (Exception e)
            {
                throw new InitException(e);
            }
        }
        if (object != null)
        {
            autoSetUtil.doAutoSetForPorter(object,autoSetMixinMap);
        }
    }


    public Class<?> getClazz()
    {
        return clazz;
    }

    Object getObj()
    {
        return object;
    }

    /**
     * 对于rest，会优先获取非{@linkplain TiedType#REST}接口。
     *
     * @param result 地址解析结果
     * @param method 请求方法
     * @return 函数接口。
     */
    public PorterOfFun getChild(UrlDecoder.Result result, PortMethod method)
    {
        PorterOfFun porterOfFun = null;

        _PortIn portIn = getPortIn();
        switch (portIn.getTiedType())
        {

            case REST:
                porterOfFun = children.get(result.funTied());
                if (porterOfFun == null)
                {
                    porterOfFun = children.get(method.name());
                }
                break;
            case Default:
                porterOfFun = children.get(result.funTied());
                break;
        }
        if (porterOfFun != null && porterOfFun.getMethodPortIn().getMethod() != method)
        {
            porterOfFun = null;
        }
        return porterOfFun;
    }

    public void start()
    {
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                porter.start();
            }
        }
        _PortStart[] starts = getStarts();
        for (int i = 0; i < starts.length; i++)
        {
            try
            {
                PorterOfFun porterOfFun = starts[i].getPorterOfFun();
                porterOfFun.getMethod().invoke(porterOfFun.getObject());
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

    }

    public void destroy()
    {
        if (mixins != null)
        {
            for (Porter porter : mixins)
            {
                porter.destroy();
            }
        }
        _PortDestroy[] ds = getDestroys();
        for (int i = 0; i < ds.length; i++)
        {
            try
            {
                PorterOfFun porterOfFun = ds[i].getPorterOfFun();
                porterOfFun.getMethod().invoke(porterOfFun.getObject());
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

    }
}
