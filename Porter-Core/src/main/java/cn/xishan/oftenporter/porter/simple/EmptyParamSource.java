package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2017/7/27.
 */
public class EmptyParamSource implements ParamSource {

    private static final ParamSource EMPTY  = new EmptyParamSource();


    public static ParamSource getEMPTY() {
        return EMPTY;
    }

    @Override
    public void setUrlResult(UrlDecoder.Result result) {

    }

    @Override
    public <T> T getParam(String name) {
        return null;
    }

    @Override
    public <T> T getNeceParam(String name, String errmsgOfEmpty) {
        return DefaultParamSource.getNeceParamUtil(this, name, errmsgOfEmpty);
    }

    @Override
    public <T> T getNeceParam(String name) {
        return DefaultParamSource.getNeceParamUtil(this, name);
    }

    @Override
    public void putNewParams(Map<String, ?> newParams) {

    }

    @Override
    public Enumeration<String> paramNames() {
        return EnumerationImpl.getEMPTY();
    }

    @Override
    public Enumeration<Map.Entry<String, Object>> params() {
        return EnumerationImpl.getEMPTY();
    }
}
