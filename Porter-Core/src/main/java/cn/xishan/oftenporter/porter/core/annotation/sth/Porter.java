package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.util.Map;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class Porter
{
    private final Logger LOGGER;

    Object object;
    Class<?> clazz;
    _PortIn portIn;

    WholeClassCheckPassableGetter wholeClassCheckPassableGetter;

    _PortStart[] starts;
    _PortDestroy[] destroys;
    /**
     * {"funTied/method"或者"method":PorterOfFun}
     */
    Map<String, PorterOfFun> childrenWithMethod;
    Porter[] mixins;

    InObj inObj;
    private AutoSetUtil autoSetUtil;

    public Porter(AutoSetUtil autoSetUtil, WholeClassCheckPassableGetter wholeClassCheckPassableGetter)
    {
        LOGGER = LogUtil.logger(Porter.class);
        this.autoSetUtil = autoSetUtil;
        this.wholeClassCheckPassableGetter = wholeClassCheckPassableGetter;
    }

    public WholeClassCheckPassableGetter getWholeClassCheckPassableGetter()
    {
        return wholeClassCheckPassableGetter;
    }

    /**
     * 获取绑定的函数：{"funTied/method"或者"method":PorterOfFun}
     *
     * @return
     */
    public Map<String, PorterOfFun> getFuns()
    {
        return childrenWithMethod;
    }


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
            autoSetUtil.doAutoSetForPorter(object, autoSetMixinMap);
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
                porterOfFun = childrenWithMethod.get(result.funTied() + "/" + method.name());
                if (porterOfFun == null)
                {
                    porterOfFun = childrenWithMethod.get(method.name());
                }
                break;
            case Default:
                porterOfFun = childrenWithMethod.get(result.funTied() + "/" + method.name());
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
//        childrenWithMethod.clear();
//        childrenWithMethod=null;
//        mixins = null;
//        destroys = null;
//        starts = null;
//        object = null;
//        clazz = null;
//        portIn = null;
//        inObj=null;
//        autoSetUtil=null;
    }
}
