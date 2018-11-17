package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.*;

import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterSync;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * 可用于{@linkplain PorterSync PorterSync}
 *
 * @author Created by https://github.com/CLovinr on 2017/6/9.
 */
public class ServletOftenObject extends OftenObject
{
    private OftenRequest oftenRequest;
    private OftenResponse oftenResponse;
    private ParamSource paramSource;

    public ServletOftenObject(HttpServletRequest request, HttpServletResponse response)
    {
        this(request, null, response, null);
    }

    public ServletOftenObject(HttpServletRequest request, HttpServletResponse response, UrlDecoder.Result result)
    {
        this(request, null, response, result);
    }

    public ServletOftenObject(HttpServletRequest request, String path, HttpServletResponse response,
            UrlDecoder.Result result)
    {
        this("", "", "", result);
        oftenRequest = new OftenServletRequest(request, path, response, PortMethod.DEFAULT);
        oftenResponse = new OftenServletResponse(response);
        paramSource = new DefaultParamSource(oftenRequest);
        paramSource.setUrlResult(result);
    }

    public ServletOftenObject(HttpServletRequest request, String path, HttpServletResponse response)
    {
        this(request, path, response, null);
    }

    private UrlDecoder.Result result;


    private ServletOftenObject(String contextName, String classTied, String funTied, UrlDecoder.Result result)
    {
        this.result = result != null ? result :
                DefaultUrlDecoder.newResult(new HashMap<>(0), contextName, classTied, funTied);
    }

    @Override
    public ParamSource getParamSource()
    {
        return paramSource;
    }

    @Override
    public boolean isInnerRequest()
    {
        return true;
    }

    @Override
    public OftenRequest getRequest()
    {
        return oftenRequest;
    }

    @Override
    public OftenResponse getResponse()
    {
        return oftenResponse;
    }

    @Override
    public <T> T fentity(int index)
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T centity(int index)
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T savedObject(String key)
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T gsavedObject(String key)
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public Delivery delivery()
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public UrlDecoder.Result url()
    {
        return result;
    }

    public void setResult(UrlDecoder.Result result)
    {
        this.result = result;
        this.paramSource.setUrlResult(this.result);
    }

    /**
     * 返回空.
     *
     * @return
     */
    @Override
    public BridgeName getPName()
    {
        return null;
    }

}
