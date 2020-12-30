package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisParams;
import cn.xishan.oftenporter.oftendb.db.ConnectionWrapper;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Consumer;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@AutoSetDefaultDealt(gen = MyBatisDaoGen.class)
public interface MyBatisDao
{

    /**
     * 会获取新的sql会话,记得手动关闭。
     *
     * @return
     */
    SqlSession getNewSqlSession();

    /**
     * 会获取新的sql会话。
     *
     * @return
     */
    <T> T mapper(Class<T> clazz);

    /**
     * 如果开启了事务，则返回当前连接；否则返回新的连接对象，<strong>注意：</strong>此时需要手动关闭。
     *
     * @return
     */
    Connection currentConnection();

    /**
     * 开启新的连接。
     *
     * @return
     */
    Connection newConnection();

    /**
     * 重新加载mybatis。
     */
    void reloadMybatis() throws Throwable;

    /**
     * 获取当前数据源。
     *
     * @return
     */
    DataSource getDataSource();

    /**
     * 设置新的数据源，会导致reload。
     *
     * @param dataSource
     * @return 之前的数据源
     * @throws Throwable
     */
    DataSource setDataSource(DataSource dataSource) throws Throwable;

    JSONObject getDataSourceConf();

    /**
     * 会设置新的数据源，会导致reload。
     *
     * @param dataSourceConf
     * @return 之前的数据源
     */
    DataSource setDataSourceConf(JSONObject dataSourceConf) throws Throwable;

    String getTableName();

    /**
     * 判断被扫描的表中是否包含指定表名。
     *
     * @param tableName
     * @return
     */
    boolean existsTable(String tableName);

    /**
     * 判断被扫描的某表中是否包含指定列名。
     *
     * @param tableName
     * @param columnName
     * @return
     */
    boolean existsTableColumn(String tableName, String columnName);

    String[] getTableColumns(String tableName);

    /**
     * 参数来源为：{@linkplain MyBatisMapper#params()}，{@linkplain MyBatisParams}，xml里的&lt;!--$json:{}--&gt;
     *
     * @return
     */
    JSONObject getJsonParams();

    static MyBatisDao getMyBatisDao(Object proxyDao)
    {
        Invocation4Dao invocationHandler = (Invocation4Dao) ProxyUtil
                .getInvocationHandler(proxyDao);
        return invocationHandler.getMyBatisDao();
    }

    /**
     * 当前数据库连接关闭后回调，若当前没有连接则立即调用。
     *
     * @param consumer
     */
    static void onCurrentTSConnectionClosed(Consumer<Void> consumer)
    {
        ConnectionWrapper.onCurrentTSConnectionClosed(consumer);
    }

    /**
     * 包装连接，用于更好地支持{@linkplain #onCurrentTSConnectionClosed(Consumer)}
     *
     * @param connection
     * @return
     */
    static Connection wrapperConnection(Connection connection)
    {
        if (!(connection instanceof ConnectionWrapper))
        {
            connection = new ConnectionWrapper(connection);
        }
        return connection;
    }
}
