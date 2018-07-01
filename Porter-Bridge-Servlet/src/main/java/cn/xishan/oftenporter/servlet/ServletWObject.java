package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.*;

import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * 可用于{@linkplain SyncPorter SyncPorter}
 *
 * @author Created by https://github.com/CLovinr on 2017/6/9.
 */
public class ServletWObject extends WObject
{
    private WRequest wRequest;
    private WResponse wResponse;
    private ParamSource paramSource;

    public ServletWObject(HttpServletRequest request, HttpServletResponse response)
    {
        this(request, null, response);
    }

    public ServletWObject(HttpServletRequest request, String path, HttpServletResponse response)
    {
        this("", "", "");
        wRequest = new WServletRequest(request, path, response, PortMethod.DEFAULT);
        wResponse = new WServletResponse(response);
        paramSource = new DefaultParamSource(wRequest);
        paramSource.setUrlResult(result);
    }

    private UrlDecoder.Result result;


    private ServletWObject(String contextName, String classTied, String funTied)
    {
        result = DefaultUrlDecoder.newResult(new HashMap<>(0), contextName, classTied, funTied);
        cn = new Object[0];
        cu = new Object[0];
        cinner = new Object[0];
        fn = new Object[0];
        fu = new Object[0];
        finner = new Object[0];
        cInNames = InNames.fromStringArray(new String[0], new String[0], new String[0]);
        fInNames = InNames.fromStringArray(new String[0], new String[0], new String[0]);
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
    public WRequest getRequest()
    {
        return wRequest;
    }

    @Override
    public WResponse getResponse()
    {
        return wResponse;
    }

    @Override
    public <T> T finObject(int index)
    {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T cinObject(int index)
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
    }

    /**
     * 返回空.
     *
     * @return
     */
    @Override
    public PName getPName()
    {
        return null;
    }

}
