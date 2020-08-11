package cn.xishan.oftenporter.oftendb.mybatis;

import org.apache.ibatis.cursor.Cursor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author Created by https://github.com/CLovinr on 2020-08-11.
 */
class CursorWrap implements Cursor
{
    private Connection connection;
    private Cursor cursor;

    public CursorWrap(Connection connection, Cursor cursor)
    {
        this.connection = connection;
        this.cursor = cursor;
    }

    @Override
    public boolean isOpen()
    {
        return cursor.isOpen();
    }

    @Override
    public boolean isConsumed()
    {
        return cursor.isConsumed();
    }

    @Override
    public int getCurrentIndex()
    {
        return cursor.getCurrentIndex();
    }

    @Override
    public void close() throws IOException
    {
        cursor.close();
        try
        {
            connection.close();
        } catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public Iterator iterator()
    {
        return cursor.iterator();
    }
}
