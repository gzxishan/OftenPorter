package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

public abstract class SqlAdvancedExecutor extends AdvancedExecutor
{

    protected abstract Object execute(Connection connection, SqlHandle sqlHandle) throws DBException;
    private static final Logger LOGGER = LogUtil.logger(SqlAdvancedExecutor.class);

    @Override
    public final Object toFinalObject()
    {
        return null;
    }

    public static AdvancedExecutor withSqlAndArgs(String sql, Object[] args)
    {
        SqlAdvancedExecutor executor = new SqlAdvancedExecutor()
        {
            @Override
            protected Object execute(Connection connection, SqlHandle sqlHandle) throws DBException
            {
                try
                {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    LOGGER.debug(sql);
                    if(args!=null){
                        for (int i = 0; i <args.length ; i++)
                        {
                            ps.setObject(i+1,args[i]);
                        }
                    }
                    return ps.executeUpdate();
                } catch (Exception e)
                {
                    throw new DBException(e);
                }
            }
        };

        return executor;
    }
}
