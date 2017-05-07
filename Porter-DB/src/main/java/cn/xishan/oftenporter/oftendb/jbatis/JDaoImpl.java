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

    private JSONObject toJson(Object[] nameValues){
        if(nameValues.length%2!=0){
            throw new RuntimeException("Illegal NameValues length:"+nameValues.length);
        }
        JSONObject jsonObject =  new JSONObject(nameValues.length/2);
        for (int i = 0; i < nameValues.length; i+=2)
        {
            String name = (String) nameValues[i];
            Object value = nameValues[i+1];
            jsonObject.put(name,value);
        }
        return jsonObject;
    }

    @Override
    public <T> T query(WObject wObject, Object... nameValues)
    {
        return query(getMethod(),toJson(nameValues),wObject);
    }

    @Override
    public <T> T query(String method, WObject wObject, Object... nameValues)
    {
        return query(method,toJson(nameValues),wObject);
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
    public <T> T execute(WObject wObject, Object... nameValues)
    {
        return execute(getMethod(),toJson(nameValues),wObject);
    }

    @Override
    public <T> T execute(String method, WObject wObject, Object... nameValues)
    {
        return execute(method,toJson(nameValues),wObject);
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
