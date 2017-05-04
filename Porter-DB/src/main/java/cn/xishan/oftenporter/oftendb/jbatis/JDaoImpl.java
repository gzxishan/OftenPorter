package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by chenyg on 2017-04-29.
 */
class JDaoImpl implements JDao
{
    private JsBridge jsBridge;

    public JDaoImpl(JsBridge jsBridge)
    {
        this.jsBridge = jsBridge;
    }

    private String getMethod()
    {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        return stacks[3].getMethodName();
    }

    @Override
    public <T> T query(JSONObject json,WObject wObject)
    {
        return query(getMethod(), json,wObject);
    }

    @Override
    public <T> T query(String method, JSONObject json,WObject wObject)
    {
        return jsBridge.query(method, json,wObject);
    }

    @Override
    public <T> T execute(JSONObject json,WObject wObject)
    {
        return execute(getMethod(), json,wObject);
    }

    @Override
    public <T> T execute(String method, JSONObject json,WObject wObject)
    {
        return jsBridge.execute(method, json,wObject);
    }

    @Override
    public SqlSource getSqlSource()
    {
        return jsBridge.sqlSource;
    }

    @Override
    public DBSource getDBSource() {
        return jsBridge.dbSource;
    }
}
