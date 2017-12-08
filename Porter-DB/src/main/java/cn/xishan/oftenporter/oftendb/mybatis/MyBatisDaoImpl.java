package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoImpl implements MyBatisDao, MSqlSessionFactoryBuilder.BuilderListener
{


    private Class<?> mapperClass;
    private MyBatisDaoGen myBatisDaoGen;
    private File mapperFile;
    private String mapperPath;
    private MyBatis.Type type;


    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen, Class<?> mapperClass)
    {
        this.myBatisDaoGen = myBatisDaoGen;
        this.mapperClass = mapperClass;
    }


    void setMapperFile(MyBatis.Type type, String mapperPath, File mapperFile)
    {
        this.type = type;
        this.mapperPath = mapperPath;
        this.mapperFile = mapperFile;
    }

    @Override
    public SqlSession getSqlSession(WObject wObject)
    {
        SqlSession sqlSession = MyBatisBridge.openSession(wObject, myBatisDaoGen.mybatisConfig);
        return sqlSession;
    }

    @Override
    public <T> T mapper(WObject wObject)
    {
        return getSqlSession(wObject).getMapper((Class<T>) mapperClass);
    }

    @Override
    public Connection getConnection()
    {
        return new ConnectionWrap(getSqlSession());
    }

    @Override
    public SqlSession getSqlSession()
    {
        SqlSession sqlSession = MyBatisBridge._openSession(null, myBatisDaoGen.mybatisConfig);

        return sqlSession;
    }

    @Override
    public <T> T mapper(Class<T> clazz)
    {
        return getSqlSession().getMapper(clazz);
    }


    @Override
    public <T> T selectOne(WObject wObject, String statement)
    {
        return getSqlSession(wObject).selectOne(statement);
    }

    @Override
    public <T> T selectOne(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement)
    {
        return getSqlSession(wObject).selectList(statement);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(WObject wObject, String statement, Object parameter, RowBounds rowBounds)
    {
        return getSqlSession(wObject).selectList(statement, parameter, rowBounds);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, String mapKey)
    {
        return getSqlSession(wObject).selectMap(statement, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey)
    {
        return getSqlSession(wObject).selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey,
            RowBounds rowBounds)
    {
        return getSqlSession(wObject).selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement)
    {
        return getSqlSession(wObject).selectCursor(statement);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter, RowBounds rowBounds)
    {
        return getSqlSession(wObject).selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public void select(WObject wObject, String statement, Object parameter, ResultHandler handler)
    {
        getSqlSession(wObject).select(statement, parameter, handler);
    }

    @Override
    public void select(WObject wObject, String statement, ResultHandler handler)
    {
        getSqlSession(wObject).selectCursor(statement, handler);
    }

    @Override
    public void select(WObject wObject, String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)
    {
        getSqlSession(wObject).select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(WObject wObject, String statement)
    {
        return getSqlSession(wObject).insert(statement);
    }

    @Override
    public int insert(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).insert(statement, parameter);
    }

    @Override
    public int update(WObject wObject, String statement)
    {
        return getSqlSession(wObject).update(statement);
    }

    @Override
    public int update(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).update(statement, parameter);
    }

    @Override
    public int delete(WObject wObject, String statement)
    {
        return getSqlSession(wObject).delete(statement);
    }

    @Override
    public int delete(WObject wObject, String statement, Object parameter)
    {
        return getSqlSession(wObject).delete(statement, parameter);
    }


    @Override
    public void onBuild() throws Exception
    {
        myBatisDaoGen.loadXml(type, mapperPath, mapperFile);
    }

    @Override
    public File getFile()
    {
        return mapperFile;
    }

    @Override
    public boolean willCheckMapperFile()
    {
        return mapperFile != null;
    }
}
