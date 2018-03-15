package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.base.WObject;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoImpl implements MyBatisDao, MSqlSessionFactoryBuilder.BuilderListener
{


    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisDaoImpl.class);
    private MyBatisDaoGen myBatisDaoGen;
    private File mapperFile;
    private String path;
    private _MyBatis myBatis;


    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen, _MyBatis myBatis, String path)
    {
        this.myBatisDaoGen = myBatisDaoGen;
        this.myBatis = myBatis;
        this.path = path;
    }

    private void checkMapperClass()
    {
        if (myBatis == null)
        {
            throw new NullPointerException("there is no mapper dao class");
        }
    }

    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen)
    {
        this.myBatisDaoGen = myBatisDaoGen;
    }


    void setMapperFile(File mapperFile)
    {
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
        checkMapperClass();
        return getSqlSession(wObject).getMapper((Class<T>) myBatis.daoClass);
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
    public Connection getConnection(WObject wObject)
    {
        return getSqlSession(wObject).getConnection();
    }

    @Override
    public <T> T mapper(WObject wObject, Class<T> clazz)
    {
        return mapperOther(getSqlSession(wObject), clazz);
    }

    private <T> T mapperOther(SqlSession sqlSession, Class<T> otherClass)
    {

        try
        {
            if (!sqlSession.getConfiguration().hasMapper(otherClass))
            {
                _MyBatisField myBatisField = new _MyBatisField();
                myBatisField.value = otherClass;
                MyBatisDaoImpl myBatisDao = myBatisDaoGen.genObject(myBatisField);
                myBatisDaoGen.mybatisConfig.mSqlSessionFactoryBuilder.regNewMapper(myBatisDao);
            }
            return sqlSession.getMapper(otherClass);
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public <T> T getMapper(Class<T> clazz)
    {
        LOGGER.debug("will not support the transaction :in {}", MyBatisBridge.class);
        return mapperOther(getSqlSession(), clazz);
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
    public void close(WObject wObject)
    {
        getSqlSession(wObject).close();
    }


    @Override
    public void onParse() throws IOException
    {
        myBatisDaoGen.loadXml(myBatis, path, mapperFile);
    }

    @Override
    public void onBindAlias()
    {
        myBatisDaoGen.bindAlias(myBatis);
    }

    @Override
    public void listenFiles(MSqlSessionFactoryBuilder.FileListener fileListener) throws Exception
    {
        fileListener.onGetFiles(new File[]{mapperFile});
        myBatis.setFileListener(fileListener);
    }

    @Override
    public boolean willCheckMapperFile()
    {
        return mapperFile != null;
    }
}
