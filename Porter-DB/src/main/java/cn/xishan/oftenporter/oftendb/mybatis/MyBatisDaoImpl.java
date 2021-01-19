package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.DBException;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

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
    private boolean isNew = true;

    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen, _MyBatis myBatis, String path)
    {
        this.myBatisDaoGen = myBatisDaoGen;
        this.myBatis = myBatis;
        this.path = path;
    }

    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen)
    {
        this.myBatisDaoGen = myBatisDaoGen;
    }


    public boolean isNew()
    {
        return isNew;
    }

    void setMapperFile(File mapperFile)
    {
        this.mapperFile = mapperFile;
    }


    @Override
    public SqlSession getNewSqlSession()
    {
        return MyBatisBridge.__getSqlSession__(myBatisDaoGen.source);
    }

    @Override
    public <T> T mapper(Class<T> clazz)
    {
        Object dao = MyBatisDaoGen.doProxy(this, clazz, myBatisDaoGen.source,
                myBatisDaoGen.moption().myBatisOption.wrapDaoThrowable);
        return (T) dao;
    }

    @Override
    public Connection currentConnection()
    {
        Connection connection;
        try
        {
            connection = MyBatisBridge.__openConnection(myBatisDaoGen.source, true);
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
        return connection;
    }

    @Override
    public Connection newConnection()
    {
        return MyBatisBridge.__openNewConnection__(myBatisDaoGen.source, true);
    }

    @Override
    public void reloadMybatis() throws Throwable
    {
        myBatisDaoGen.moption().mSqlSessionFactoryBuilder
                .reload(myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getDataSource());
    }

    @Override
    public DataSource getDataSource()
    {
        return myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getDataSource();
    }

    @Override
    public DataSource setDataSource(DataSource dataSource) throws Throwable
    {
        return myBatisDaoGen.moption().mSqlSessionFactoryBuilder.setDataSource(dataSource);
    }

    @Override
    public JSONObject getDataSourceConf()
    {
        return myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getDataSourceConf();
    }

    @Override
    public DataSource setDataSourceConf(JSONObject dataSourceConf) throws Throwable
    {
        return myBatisDaoGen.moption().mSqlSessionFactoryBuilder.setDataSourceConf(dataSourceConf);
    }

    @Override
    public String getTableName()
    {
        return myBatis == null ? null : myBatis.getTableName();
    }

    @Override
    public String[] getTableColumns(String tableName)
    {
        Set<String> set = myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getCachedTableColumns(tableName);
        return set == null ? null : set.toArray(new String[0]);
    }


    @Override
    public boolean existsTable(String tableName)
    {
        Set<String> set = myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getCachedTableColumns(tableName);
        return set != null && set.size() > 0;
    }

    @Override
    public boolean existsTableColumn(String tableName, String columnName)
    {
        Set<String> set = myBatisDaoGen.moption().mSqlSessionFactoryBuilder.getCachedTableColumns(tableName);
        return set != null && set.contains(columnName);
    }

    @Override
    public JSONObject getJsonParams()
    {
        return myBatis == null ? null : myBatis.getJsonParams();
    }

    Object getMapperDao(SqlSession sqlSession, Class<?> otherClass) throws Exception
    {
        if (!sqlSession.getConfiguration().hasMapper(otherClass))
        {
            _MyBatisField myBatisField = new _MyBatisField();
            myBatisField.value = otherClass;
            MyBatisDaoImpl myBatisDao = myBatisDaoGen.genObject(myBatisField);
            myBatisDaoGen.moption().mSqlSessionFactoryBuilder.regNewMapper(myBatisDao);
        }
        return sqlSession.getMapper(otherClass);
    }


    @Override
    public void onParse() throws IOException
    {
        this.isNew = false;
        myBatisDaoGen.loadXml(myBatis, path, mapperFile);
    }

    @Override
    public void onBindAlias()
    {
        this.isNew = false;
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
