package cn.xishan.oftenporter.demo.oftendb.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import cn.xishan.oftenporter.oftendb.data.impl.MysqlSource;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.sql.SqlHandle;
import cn.xishan.oftenporter.porter.core.util.WPTool;

public class SqlDBSource extends MysqlSource
{

    public SqlDBSource()
    {
        super((wObject, configed, configing) -> configing.setCollectionName("test1"));
        Connection connection = null;
        Statement statement = null;
        try
        {
            String initSql =
                    "CREATE TABLE IF NOT EXISTS `test1` (\n" + "  `_id` char(32) NOT NULL,\n"
                            + "  `name` varchar(35) NOT NULL,\n"
                            + "  `age` int(11) NOT NULL,\n"
                            + "  `sex` varchar(2) NOT NULL,\n"
                            + "  `time` datetime NOT NULL,\n"
                            + "  PRIMARY KEY (`_id`)\n"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            connection = getConn();
             statement = connection.createStatement();
            statement.execute(initSql);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            WPTool.close(statement);
            WPTool.close(connection);
        }
    }

    @Override
    public Connection getConnection()
    {
        return getConn();
    }

    private Connection getConn()
    {

        try
        {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:h2:~/PorterDemo/oftendb;MODE=MySQL", "sa", "");
            return conn;
        } catch (Exception e)
        {
            throw new DBException(e);
        }

    }

    @Override
    public DBHandle getDBHandle()
            throws DBException
    {
        SqlHandle sqlHandle = new SqlHandle(getConn());
        return sqlHandle;
    }

}
