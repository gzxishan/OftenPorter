package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.Common;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONObject;

import javax.script.Invocable;
import java.sql.Connection;

/**
 * Created by chenyg on 2017-04-29.
 */
class JsBridge
{

    private Invocable invocable;
    private SqlSource sqlSource;

    public JsBridge(Invocable invocable, SqlSource sqlSource)
    {
        this.invocable = invocable;
        this.sqlSource = sqlSource;
    }

    private Connection getConn(WObject wObject)
    {
        SqlSource source = Common.getSqlSource(wObject);
        if (source == null)
        {
            source = sqlSource;
        }
        return source.getConnection();
    }

    public <T> T query(String method, JSONObject json, WObject wObject)
    {
        try
        {
            T t = (T) invocable.invokeFunction(method, getConn(wObject), json);
            return t;
        } catch (Exception e)
        {
            throw new JQueryException(e);
        }
    }

    public <T> T execute(String method, JSONObject json, WObject wObject)
    {
        try
        {
            T t = (T) invocable.invokeFunction(method, getConn(wObject), json);
            return t;
        } catch (Exception e)
        {
            throw new JExecuteException(e);
        }
    }
}
