package cn.xishan.oftenporter.oftendb.jbatis;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/3.
 */
public interface BlobData extends Closeable
{
    Blob getBlob();

    long length() throws Exception;

    InputStream getInputStream() throws Exception;

    /**
     * @param pos    从1开始。
     * @param length
     * @return
     */
    InputStream getInputStream(long pos, long length) throws Exception;

    /**
     * @param pos 从1开始。
     * @return
     */
    OutputStream getOutputStream(long pos) throws Exception;
}
