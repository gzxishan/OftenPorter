package cn.xishan.oftenporter.demo.oftendb.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import cn.xishan.oftenporter.oftendb.data.DataAble;
import cn.xishan.oftenporter.oftendb.data.ParamsGetter;
import cn.xishan.oftenporter.oftendb.data.impl.MysqlSource;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlHandle;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

public class SqlDBSource extends MysqlSource {

    public SqlDBSource() {
        Connection connection = null;
        try {
            String initSql =
                    "CREATE TABLE IF NOT EXISTS `test1` (\n" + "  `_id` char(32) NOT NULL,\n"
                            + "  `name` varchar(35) NOT NULL,\n"
                            + "  `age` int(11) NOT NULL,\n"
                            + "  `sex` varchar(2) NOT NULL,\n"
                            + "  `time` datetime NOT NULL,\n"
                            + "  PRIMARY KEY (`_id`)\n"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            connection = getConn();
            Statement statement = connection.createStatement();
            statement.execute(initSql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Connection getConnection()
    {
        return getConn();
    }

    private Connection getConn() {

        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:h2:~/PorterDemo/oftendb;MODE=MySQL", "sa", "");
            return conn;
        } catch (Exception e) {
            throw new DBException(e);
        }

    }

    @Override
    public DBHandle getDbHandle(ParamsGetter paramsGetter, @MayNull DataAble dataAble, DBHandle dbHandle)
            throws DBException {
        SqlHandle sqlHandle = (SqlHandle) dbHandle;
        if (sqlHandle == null) {
            sqlHandle = new SqlHandle(getConn(), null);
        }
        if (dataAble != null) {
            sqlHandle.setTableName(dataAble.getCollectionName());
        }
        return sqlHandle;
    }

}
