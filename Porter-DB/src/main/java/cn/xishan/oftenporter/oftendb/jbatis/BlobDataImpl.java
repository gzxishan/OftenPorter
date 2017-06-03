package cn.xishan.oftenporter.oftendb.jbatis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/3.
 */
class BlobDataImpl implements BlobData
{

    private Connection connection;
    private Blob blob;

    public BlobDataImpl(Connection connection, Blob blob)
    {
        this.connection = connection;
        this.blob = blob;
    }

    @Override
    public Blob getBlob()
    {
        return blob;
    }

    @Override
    public InputStream getInputStream() throws Exception
    {
        return blob.getBinaryStream();
    }

    @Override
    public long length() throws Exception
    {
        return blob.length();
    }

    @Override
    public InputStream getInputStream(long pos, long length) throws Exception
    {
        return blob.getBinaryStream(pos,length);
    }

    @Override
    public OutputStream getOutputStream(long pos) throws Exception
    {
        return blob.setBinaryStream(pos);
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            connection.close();
        } catch (SQLException e)
        {
            throw new IOException(e);
        }
    }
}
