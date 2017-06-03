package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import javax.script.Invocable;

/**
 * Created by chenyg on 2017-04-29.
 */
class JsBridge
{
    String path;
    Invocable invocable;
    SqlSource sqlSource;
    DBSource dbSource;
    Logger LOGGER;

    public JsBridge(Invocable invocable, DBSource dbSource, SqlSource sqlSource,String path,Logger sqlSourceLogger)
    {
        this.invocable = invocable;
        this.dbSource = dbSource;
        this.sqlSource = sqlSource;
        this.path=path;
        LOGGER=sqlSourceLogger;
    }


    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        JDaoGen.doFinalize();
    }

    private _SqlSorce getConn(WObject wObject)
    {
        return new _SqlSorce(dbSource,sqlSource, wObject,LOGGER);
    }

    public <T> T query(String method, JSONObject json, WObject wObject)
    {
        try
        {
            LOGGER.debug("jbatis script:{}",path);
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
            LOGGER.debug("jbatis script:{}",path);
            T t = (T) invocable.invokeFunction(method, json, getConn(wObject));
            return t;
        } catch (Exception e)
        {
            throw new JExecuteException(e);
        }
    }
}
