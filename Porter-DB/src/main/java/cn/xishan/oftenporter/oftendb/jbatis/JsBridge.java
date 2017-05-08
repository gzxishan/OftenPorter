package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.Common;
import cn.xishan.oftenporter.oftendb.data.DBSource;
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

    Invocable invocable;
    SqlSource sqlSource;
    DBSource dbSource;

    public JsBridge(Invocable invocable, DBSource dbSource, SqlSource sqlSource)
    {
        this.invocable = invocable;
        this.dbSource = dbSource;
        this.sqlSource = sqlSource;
    }


    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        JDaoGen.doFinalize();
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
            T t = (T) invocable.invokeFunction(method, json, getConn(wObject));
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
            T t = (T) invocable.invokeFunction(method, json, getConn(wObject));
            return t;
        } catch (Exception e)
        {
            throw new JExecuteException(e);
        }
    }
}
