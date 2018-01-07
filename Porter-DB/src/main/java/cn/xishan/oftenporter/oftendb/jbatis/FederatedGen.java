package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.FederatedMysqlOption;
import cn.xishan.oftenporter.oftendb.annotation.FederatedOption;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.sql.Connection;

import java.sql.Statement;
import java.util.List;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
class FederatedGen implements AutoSetGen
{
    @AutoSet(nullAble = true)
    DBSource dbSource;
    @AutoSet
    Logger LOGGER;


    static class FederatedImplInner extends Federated.Adapter
    {

        private boolean isAutoInit;


        public FederatedImplInner(boolean isAutoInit)
        {
            this.isAutoInit = isAutoInit;
        }

        @Override
        public boolean isAutoInit()
        {
            return isAutoInit;
        }

        @Override
        public void doInit()
        {

        }

        private void checkEmpty(Object object, String err)
        {
            if (WPTool.isEmpty(object))
            {
                throw new NullPointerException(err + " is empty!");
            }
        }

        @Override
        public void init(DBSource dbSource, boolean dropTableIfExists, String tableName, String jdbcUrl,
                String driverClass, String connectionUrl)
        {

            checkEmpty(tableName, "table name");
            checkEmpty(jdbcUrl, "jdbc url");
            checkEmpty(driverClass, "driver class");
            checkEmpty(connectionUrl, "connection url");


            SqlSource sqlSource = (SqlSource) dbSource;

            Connection connection = null;

            try
            {
                List<SqlUtil.CreateTable> createTableList = SqlUtil
                        .exportCreateTable(tableName, jdbcUrl, driverClass);

                connection = sqlSource.getConnection();
                connection.setAutoCommit(false);
                Statement statement = connection.createStatement();

                for (SqlUtil.CreateTable createTable : createTableList)
                {
                    if (dropTableIfExists)
                    {
                        statement.addBatch("DROP TABLE IF EXISTS `" + createTable.tableName + "`");
                    }
                    int index0 = createTable.createTableSql.indexOf("ENGINE=");
                    int index1 = createTable.createTableSql.indexOf(" ", index0);
                    if (index1 == -1)
                    {
                        index1 = createTable.createTableSql.length();
                    }
                    StringBuilder sqls = new StringBuilder();
                    sqls.append(createTable.createTableSql.substring(0, index0));
                    sqls.append("ENGINE=FEDERATED ").append(createTable.createTableSql.substring(index1));
                    sqls.append(" CONNECTION='").append(connectionUrl).append("'");
                    statement.addBatch(sqls.toString());
                }
                statement.executeBatch();
                statement.close();
                connection.commit();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            } finally
            {
                WPTool.close(connection);
            }


        }

        @Override
        public void initOfMysql(DBSource dbSource, boolean dropTableIfExists, String tableName, String host,
                String dbname, String user,
                String password)
        {
            checkEmpty(tableName, "table name");
            checkEmpty(host, "host");
            checkEmpty(dbname, "database name");
            checkEmpty(user, "user name");
            try
            {
                String url = "jdbc:mysql://" + host + "/" + dbname + "?user=" + URLEncoder
                        .encode(user, "utf-8") + "&password=" + URLEncoder.encode(password, "utf-8");
                String connectionUrl = "mysql://" + URLEncoder.encode(user, "utf-8") + ":" + URLEncoder
                        .encode(password, "utf-8") + "@" + host + "/" + dbname + "/" + tableName;
                init(dbSource, dropTableIfExists, tableName, url, "com.mysql.jdbc.Driver", connectionUrl);
            } catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    class FederatedImpl implements Federated
    {
        private boolean isMySql;
        private int tryCount;
        private int tryDelay;
        private Federated federated;
        private DBSource dbSource;
        private boolean dropTableIfExists;
        private String tableName;
        private String jdbcUrl;
        private String driverClass;
        private String connectionUrl;
        boolean hasStart = false;


        private String host;
        private String dbname;
        private String user;
        private String password;
        private IOnStart onStart;

        @Override
        public IOnStart getOnStartListener()
        {
            return onStart;
        }

        @Override
        public void setOnStartListener(IOnStart onStartListener)
        {
            this.onStart = onStartListener;
            if (hasStart && onStartListener != null)
            {
                onStartListener.onStart(this);
            }
        }

        public void setTryCount(int tryCount)
        {
            this.tryCount = tryCount;
        }

        public void setTryDelay(int tryDelay)
        {
            this.tryDelay = tryDelay;
        }

        public void setDBSource(DBSource dbSource)
        {
            this.dbSource = dbSource;
        }

        public void setDropTableIfExists(boolean dropTableIfExists)
        {
            this.dropTableIfExists = dropTableIfExists;
        }

        public void setTableName(String tableName)
        {
            this.tableName = tableName;
        }

        public void setJdbcUrl(String jdbcUrl)
        {
            this.jdbcUrl = jdbcUrl;
        }

        public void setDriverClass(String driverClass)
        {
            this.driverClass = driverClass;
        }

        public void setConnectionUrl(String connectionUrl)
        {
            this.connectionUrl = connectionUrl;
        }

        public void setHost(String host)
        {
            this.host = host;
        }

        public void setDBName(String dbname)
        {
            this.dbname = dbname;
        }

        public void setUser(String user)
        {
            this.user = user;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public FederatedImpl(int tryCount, int tryDelay, boolean isAutoInit)
        {
            if (tryCount < 0)
            {
                tryCount = 0;
            }
            this.tryCount = tryCount + 1;
            this.tryDelay = tryDelay;
            federated = new FederatedImplInner(isAutoInit);
        }

        @Override
        public boolean isAutoInit()
        {
            return federated.isAutoInit();
        }

        @Override
        public void doInit()
        {
            if (isMySql)
            {
                initOfMysql();
            } else
            {
                init();
            }
        }

        @Override
        public void init(DBSource dbSource, boolean dropTableIfExists, String tableName, String jdbcUrl,
                String driverClass,
                String connectionUrl)
        {
            isMySql = false;
            this.dbSource = dbSource;
            this.dropTableIfExists = dropTableIfExists;
            this.tableName = tableName;
            this.jdbcUrl = jdbcUrl;
            this.driverClass = driverClass;
            this.connectionUrl = connectionUrl;
        }

        private void init()
        {

            int tryCount = this.tryCount;
            while (tryCount-- > 0)
            {
                try
                {
                    LOGGER.debug("init federated[{},{}]...", tableName, jdbcUrl);
                    federated.init(dbSource, dropTableIfExists, tableName, jdbcUrl, driverClass, connectionUrl);
                    LOGGER.debug("init federated[{},{}] success!", tableName, jdbcUrl);
                    break;
                } catch (Exception e)
                {
                    LOGGER.debug("init federated[{},{}] fail!", tableName, jdbcUrl);
                    LOGGER.debug(e.getMessage(), e);
                    if (tryCount <= 0)
                    {
                        break;
                    }
                    if (tryDelay > 0)
                    {
                        try
                        {
                            Thread.sleep(tryDelay);
                        } catch (InterruptedException e1)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

        @Override
        public void initOfMysql(DBSource dbSource, boolean dropTableIfExists, String tableName, String host,
                String dbname,
                String user, String password)
        {
            isMySql = true;
            this.dbSource = dbSource;
            this.dropTableIfExists = dropTableIfExists;
            this.tableName = tableName;
            this.host = host;
            this.dbname = dbname;
            this.user = user;
            this.password = password;

        }

        private void initOfMysql()
        {
            int tryCount = this.tryCount;
            while (tryCount-- > 0)
            {
                try
                {
                    LOGGER.debug("init federated[{},{},{},{}]...", host, user, dbname, tableName);
                    federated.initOfMysql(dbSource, dropTableIfExists, tableName, host,
                            dbname, user, password);
                    LOGGER.debug("init federated[{},{},{},{}] success!", host, user, dbname, tableName);
                    break;
                } catch (Exception e)
                {
                    LOGGER.debug("init federated[{},{},{},{}] fail!", host, user, dbname, tableName);
                    LOGGER.debug(e.getMessage(), e);
                    if (tryCount <= 0)
                    {
                        break;
                    }
                    if (tryDelay > 0)
                    {
                        try
                        {
                            Thread.sleep(tryDelay);
                        } catch (InterruptedException e1)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }


    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option) throws Exception
    {
        FederatedImpl federated;
        JSONObject params;
        if (field.isAnnotationPresent(FederatedOption.class))
        {
            if (dbSource == null)
            {
                throw new DBException("dbSource is not set!");
            }
            FederatedOption federatedOption = field.getAnnotation(FederatedOption.class);

            FederatedOption federatedOptionOfClass = AnnoUtil.getAnnotation(currentObjectClass, FederatedOption.class);

            int dropIfExists = federatedOptionOfClass != null && federatedOptionOfClass
                    .dropIfExists() != -1 ? federatedOptionOfClass.dropIfExists() : federatedOption.dropIfExists();
            String tableName = federatedOptionOfClass != null && WPTool
                    .notNullAndEmpty(federatedOptionOfClass.tableName()) ? federatedOptionOfClass
                    .tableName() : federatedOption.tableName();
            String jdbcUrl = federatedOptionOfClass != null && WPTool
                    .notNullAndEmpty(federatedOptionOfClass.jdbcUrl()) ? federatedOptionOfClass
                    .jdbcUrl() : federatedOption.jdbcUrl();
            String driverClass = federatedOptionOfClass != null && WPTool
                    .notNullAndEmpty(federatedOptionOfClass.driverClass()) ? federatedOptionOfClass
                    .driverClass() : federatedOption.driverClass();
            String connectionUrl = federatedOptionOfClass != null && WPTool
                    .notNullAndEmpty(federatedOptionOfClass.connectionUrl()) ? federatedOptionOfClass
                    .connectionUrl() : federatedOption.connectionUrl();

            int tryCount = federatedOptionOfClass != null && federatedOptionOfClass
                    .tryCount() != 0 ? federatedOptionOfClass.tryCount() : federatedOption.tryCount();
            int tryDelay = federatedOptionOfClass != null ? federatedOptionOfClass.tryDelay() : federatedOption
                    .tryDelay();

            int isAutoInit = federatedOptionOfClass != null && federatedOptionOfClass
                    .isAutoInit() != -1 ? federatedOptionOfClass.isAutoInit() : federatedOption.isAutoInit();


            federated = new FederatedImpl(tryCount, tryDelay, isAutoInit != 0);

            params = WPTool
                    .fromArray2JSON("dropIfExists", dropIfExists != 0, "tableName", tableName, "jdbcUrl", jdbcUrl,
                            "driverClass", driverClass, "connectionUrl", connectionUrl);
            isMySql = false;
        } else if (field.isAnnotationPresent(FederatedMysqlOption.class))
        {
            if (dbSource == null)
            {
                throw new DBException("dbSource is not set!");
            }
            FederatedMysqlOption mysqlOption = field.getAnnotation(FederatedMysqlOption.class);
            FederatedMysqlOption mysqlOptionOfClass = AnnoUtil
                    .getAnnotation(currentObjectClass, FederatedMysqlOption.class);


            int dropIfExists = mysqlOptionOfClass != null && mysqlOptionOfClass
                    .dropIfExists() != -1 ? mysqlOptionOfClass.dropIfExists() : mysqlOption.dropIfExists();
            String tableName = mysqlOptionOfClass != null && WPTool
                    .notNullAndEmpty(mysqlOptionOfClass.tableName()) ? mysqlOptionOfClass
                    .tableName() : mysqlOption.tableName();
            String host = mysqlOptionOfClass != null && WPTool
                    .notNullAndEmpty(mysqlOptionOfClass.host()) ? mysqlOptionOfClass
                    .host() : mysqlOption.host();
            String dbName = mysqlOptionOfClass != null && WPTool
                    .notNullAndEmpty(mysqlOptionOfClass.dbName()) ? mysqlOptionOfClass
                    .dbName() : mysqlOption.dbName();
            String user = mysqlOptionOfClass != null && WPTool
                    .notNullAndEmpty(mysqlOptionOfClass.user()) ? mysqlOptionOfClass
                    .user() : mysqlOption.user();
            String password = mysqlOptionOfClass != null && WPTool
                    .notNullAndEmpty(mysqlOptionOfClass.password()) ? mysqlOptionOfClass
                    .password() : mysqlOption.password();


            int tryCount = mysqlOptionOfClass != null && mysqlOptionOfClass
                    .tryCount() != 0 ? mysqlOptionOfClass.tryCount() : mysqlOption.tryCount();
            int tryDelay = mysqlOptionOfClass != null ? mysqlOptionOfClass.tryDelay() : mysqlOption.tryDelay();

            int isAutoInit = mysqlOptionOfClass != null && mysqlOptionOfClass
                    .isAutoInit() != -1 ? mysqlOptionOfClass.isAutoInit() : mysqlOption.isAutoInit();

            federated = new FederatedImpl(tryCount, tryDelay, isAutoInit != 0);
            params = WPTool.fromArray2JSON("dropIfExists", dropIfExists != 0, "tableName", tableName,
                    "host", host, "dbName", dbName, "user", user, "password", password);
            isMySql = true;
        } else
        {
            throw new InitException(field + " not annotated with @" + FederatedOption.class
                    .getSimpleName() + " or @" + FederatedMysqlOption.class.getSimpleName());
        }

        if (isMySql)
        {
            federated.initOfMysql(dbSource, params.getBooleanValue("dropIfExists"), params.getString("tableName"),
                    params.getString("host"),
                    params.getString("dbName"), params.getString("user"), params.getString("password"));
        } else
        {
            federated.init(dbSource, params.getBooleanValue("dropIfExists"), params.getString("tableName"),
                    params.getString("jdbcUrl"),
                    params.getString("driverClass"), params.getString("connectionUrl"));
        }


        this.federated = federated;

        return federated;
    }

    private FederatedImpl federated;
    private boolean isMySql = false;


    @PortIn.PortStart(order = 100150)
    public void onStart()
    {
        if (federated == null)
        {
            return;
        }
        if (federated.isAutoInit())
        {
            federated.doInit();
        } else if (federated.getOnStartListener() != null)
        {
            federated.hasStart = true;
            federated.getOnStartListener().onStart(federated);
        } else
        {
            federated.hasStart = true;
        }
        federated = null;
    }
}
