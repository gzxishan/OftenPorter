package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.FederatedMysqlOption;
import cn.xishan.oftenporter.oftendb.annotation.FederatedOption;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;

/**
 * 用于跨数据库连接.当使用{@linkplain FederatedOption}或{@linkplain FederatedMysqlOption}时,同时必须有{@linkplain DBSource}注入.
 *
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
@AutoSetDefaultDealt(gen = FederatedGen.class)
public interface Federated
{
    interface IOnStart{
        void onStart(Federated federated);
    }

    void init(DBSource dbSource, boolean dropTableIfExists, String tableName, String jdbcUrl, String driverClass,
            String connectionUrl);

    void initOfMysql(DBSource dbSource, boolean dropTableIfExists, String tableName, String host, String dbname,
            String user, String password);

    boolean isAutoInit();

    void doInit();

    public void setMySql(boolean mySql);

    public void setTryCount(int tryCount);

    public void setTryDelay(int tryDelay);

    public void setDbSource(DBSource dbSource);

    public void setDropTableIfExists(boolean dropTableIfExists);

    public void setTableName(String tableName);

    public void setJdbcUrl(String jdbcUrl);

    public void setDriverClass(String driverClass);

    public void setConnectionUrl(String connectionUrl);

    public void setHost(String host);

    public void setDbname(String dbname);

    public void setUser(String user);

    public void setPassword(String password);

    void setOnStartListener(IOnStart onStartListener);

    IOnStart getOnStartListener();


    abstract class Adapter implements Federated{


        @Override
        public IOnStart getOnStartListener()
        {
            return null;
        }

        @Override
        public void setOnStartListener(IOnStart onStartListener)
        {

        }

        @Override
        public void setMySql(boolean mySql)
        {

        }

        @Override
        public void setTryCount(int tryCount)
        {

        }

        @Override
        public void setTryDelay(int tryDelay)
        {

        }

        @Override
        public void setDbSource(DBSource dbSource)
        {

        }

        @Override
        public void setDropTableIfExists(boolean dropTableIfExists)
        {

        }

        @Override
        public void setTableName(String tableName)
        {

        }

        @Override
        public void setJdbcUrl(String jdbcUrl)
        {

        }

        @Override
        public void setDriverClass(String driverClass)
        {

        }

        @Override
        public void setConnectionUrl(String connectionUrl)
        {

        }

        @Override
        public void setHost(String host)
        {

        }

        @Override
        public void setDbname(String dbname)
        {

        }

        @Override
        public void setUser(String user)
        {

        }

        @Override
        public void setPassword(String password)
        {

        }
    }
}
