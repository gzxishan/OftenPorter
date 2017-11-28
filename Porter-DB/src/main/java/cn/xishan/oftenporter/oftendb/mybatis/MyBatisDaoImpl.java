package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.base.WObject;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoImpl implements MyBatisDao
{
    private Class<?> mapperClass;

    public MyBatisDaoImpl( Class<?> mapperClass)
    {
        this.mapperClass = mapperClass;
    }

    @Override
    public <T> T mapper(WObject wObject)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.getMapper((Class<T>) mapperClass);
    }

    @Override
    public <T> T selectOne(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectOne(statement);
    }

    @Override
    public <T> T selectOne(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectList(statement);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement, Object parameter, RowBounds rowBounds)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectList(statement, parameter, rowBounds);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, String mapKey)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectMap(statement, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey,
            RowBounds rowBounds)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectCursor(statement);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter, RowBounds rowBounds)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public void select(WObject wObject, String statement, Object parameter, ResultHandler handler)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        sqlSession.select(statement, parameter, handler);
    }

    @Override
    public void select(WObject wObject, String statement, ResultHandler handler)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        sqlSession.selectCursor(statement, handler);
    }

    @Override
    public void select(WObject wObject, String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        sqlSession.select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.insert(statement);
    }

    @Override
    public int insert(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.insert(statement, parameter);
    }

    @Override
    public int update(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.update(statement);
    }

    @Override
    public int update(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.update(statement, parameter);
    }

    @Override
    public int delete(WObject wObject, String statement)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.delete(statement);
    }

    @Override
    public int delete(WObject wObject, String statement, Object parameter)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession.delete(statement, parameter);
    }

    @Override
    public SqlSession getSqlSession(WObject wObject)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject);
        return sqlSession;
    }
}
