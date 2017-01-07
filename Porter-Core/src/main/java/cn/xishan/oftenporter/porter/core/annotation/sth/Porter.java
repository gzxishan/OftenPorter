package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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

    InObj inObj;
    private AutoSetUtil autoSetUtil;

    public Porter(AutoSetUtil autoSetUtil)
    {
        this.autoSetUtil = autoSetUtil;
    }

    public Map<String, PorterOfFun> getChildren()
    {
        return children;
    }

    public _PortStart[] getStarts()
    {
        return starts;
    }

    public _PortDestroy[] getDestroys()
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


    void doAutoSet()
    {
        if (object != null)
        {
            autoSetUtil.doAutoSet(object);
        }
    }


    public Class<?> getClazz()
    {
        return clazz;
    }

    public Object getObject()
    {
        if (object == null)
        {
            try
            {
                object = WPTool.newObject(clazz);
                doAutoSet();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
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
                if (portIn.isMultiTiedType())
                {
                    porterOfFun = children.get(result.funTied());
                    if (porterOfFun == null)
                    {
                        porterOfFun = children.get(method.name());
                    }
                } else
                {
                    porterOfFun = children.get(method.name());
                }
                break;
            case Default:
                porterOfFun = children.get(result.funTied());
                break;
        }
        if (porterOfFun != null && porterOfFun.getPortIn().getMethod() != method)
        {
            porterOfFun = null;
        }
        return porterOfFun;
    }
}
