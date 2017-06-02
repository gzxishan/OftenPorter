package cn.xishan.oftenporter.oftendb.jbatis;

import java.io.Closeable;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/3.
 */
public interface BlobData extends Closeable
{
    InputStream getInputStream() throws SQLException;
}
