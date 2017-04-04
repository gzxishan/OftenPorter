package cn.xishan.oftenporter.oftendb.db;


/**
 * @author Created by https://github.com/CLovinr on 2017/4/5.
 */
public interface DBEnumeration<E>
{
    boolean hasMoreElements() throws DBException;

    E nextElement() throws DBException;

    void close() throws DBException;
}
