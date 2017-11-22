package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.FederatedMysqlOption;
import cn.xishan.oftenporter.oftendb.annotation.FederatedOption;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.WPTool;

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

    static class FederatedImpl implements Federated
    {

        @Override
        public void init(DBSource dbSource, boolean dropTableIfExists, String tableName, String jdbcUrl,
                String driverClass, String connectionUrl)
        {
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

    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option) throws Exception
    {
        Federated federated = new FederatedImpl();

        if (field.isAnnotationPresent(FederatedOption.class))
        {
            if (dbSource == null)
            {
                throw new DBException("dbSource is not set!");
            }
            FederatedOption federatedOption = field.getAnnotation(FederatedOption.class);
            federated.init(dbSource, federatedOption.dropIfExists(), federatedOption.tableName(),
                    federatedOption.jdbcUrl(),
                    federatedOption.driverClass(), federatedOption.connectionUrl());
        } else if (field.isAnnotationPresent(FederatedMysqlOption.class))
        {
            if (dbSource == null)
            {
                throw new DBException("dbSource is not set!");
            }
            FederatedMysqlOption mysqlOption = field.getAnnotation(FederatedMysqlOption.class);
            federated.initOfMysql(dbSource, mysqlOption.dropIfExists(), mysqlOption.tableName(), mysqlOption.host(),
                    mysqlOption.dbName(), mysqlOption.user(), mysqlOption.password());
        }
        return federated;
    }
}
