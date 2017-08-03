package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;
import cn.xishan.oftenporter.porter.simple.EmptyParamSource;

import java.util.HashMap;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/27.
 */
public class EmptyWObject extends WObject {
    private UrlDecoder.Result result;

    public EmptyWObject() {
        this("", "", "");
    }

    public EmptyWObject(String contextName, String classTied, String funTied) {
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
    public ParamSource getParamSource() {
        return EmptyParamSource.getEMPTY();
    }

    @Override
    public WRequest getRequest() {
        return null;
    }

    @Override
    public WResponse getResponse() {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T finObject(int index) {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T cinObject(int index) {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T savedObject(String key) {
        throw new RuntimeException("not allowed");
    }

    @Override
    public <T> T gsavedObject(String key) {
        throw new RuntimeException("not allowed");
    }

    @Override
    public Delivery delivery() {
        throw new RuntimeException("not allowed");
    }

    @Override
    public UrlDecoder.Result url() {
        return result;
    }

    /**
     * 返回空.
     *
     * @return
     */
    @Override
    public PName getPName() {
        return null;
    }
}
