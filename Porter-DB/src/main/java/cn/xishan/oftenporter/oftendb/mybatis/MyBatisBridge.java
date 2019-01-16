package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.ConnectionWrapper;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionDBHandle;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisBridge
{

    private static MybatisConfig mybatisConfig;

    static
    {
        AnnoUtil.Advance.addWhite(IDynamicAnnotationImprovableForDao.class.getName());
    }

    static void start(IConfigData configData)
    {
        if (mybatisConfig == null)
        {
            throw new InitException("not init!");
        }
        mybatisConfig.start(configData);
    }

    static void destroy()
    {
        mybatisConfig.destroy();
    }

    static MybatisConfig.MOption getMOption(String source)
    {
        if (mybatisConfig == null)
        {
            throw new InitException("not init!");
        }
        MybatisConfig.MOption mOption = mybatisConfig.getOption(source);
        if (mOption == null)
        {
            throw new InitException("not found dbSource:" + source);
        }
        return mOption;
    }

    /**
     * 见{@linkplain MyBatisOption#dataSource}
     *
     * @param propertiesJson dsType{@linkplain DataSource}实现类.
     * @return
     */
    public static DataSource buildDataSource(JSONObject propertiesJson)
    {
        JSONObject json = propertiesJson;
        propertiesJson = new JSONObject();
        propertiesJson.putAll(json);
        String dsType = (String) propertiesJson.remove("dsType");
        if (OftenTool.isEmpty(dsType))
        {
            dsType = (String) propertiesJson.remove("type");
        }
        Properties properties = new Properties();
        for (String key : propertiesJson.keySet())
        {
            if (key.endsWith("--ignore"))
            {
                continue;
            }
            properties.setProperty(key, propertiesJson.getString(key));
        }
        return buildDataSource(dsType, properties);
    }


    private static class TempFactory extends UnpooledDataSourceFactory
    {
        public TempFactory(DataSource dataSource)
        {
            super.dataSource = dataSource;
        }
    }

    /**
     * @param dataSourceClass {@linkplain DataSource}实现类.
     * @param properties
     * @return
     */
    public static DataSource buildDataSource(String dataSourceClass, Properties properties)
    {
        try
        {
            Class<?> clazz = PackageUtil.newClass(dataSourceClass, null);
            DataSource dataSource = (DataSource) OftenTool.newObject(clazz);
            DataSourceFactory factory = new TempFactory(dataSource);
            factory.setProperties(properties);
            return factory.getDataSource();
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public static void init(PorterConf porterConf, MyBatisOption myBatisOption, String resourcePath) throws IOException
    {
        InputStream in = Resources.getResourceAsStream(resourcePath);
        init(porterConf, myBatisOption, in);
    }

    public synchronized static void init(PorterConf porterConf, MyBatisOption myBatisOption, InputStream configStream)
    {
        if (myBatisOption == null)
        {
            throw new NullPointerException(MyBatisOption.class.getSimpleName() + " is null!");
        }
        if (OftenTool.isEmptyOfAll(myBatisOption.dataSource, myBatisOption.dataSourceObject,
                myBatisOption.dataSourceProperPrefix))
        {
            throw new IllegalArgumentException("dataSource is empty!");
        }
        try
        {

            byte[] configData = FileTool.getData(configStream, 1024);
            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder = new MSqlSessionFactoryBuilder(
                    myBatisOption, configData);
            if (mybatisConfig == null)
            {
                mybatisConfig = new MybatisConfig();
            }

            if (myBatisOption.resourcesDir != null && !myBatisOption.resourcesDir.endsWith("/"))
            {
                myBatisOption.resourcesDir += "/";
            }
            porterConf.addContextAutoSet(myBatisOption.iConnectionBridge);
            mybatisConfig.put(myBatisOption, mSqlSessionFactoryBuilder);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @KeepFromProguard
    static ConnectionWrap __openConnection(String source) throws SQLException
    {
        return __openConnection(source, true);
    }

    /**
     * @param source
     * @param openNew 如果当前threadLocal没有，是否开启新的。
     * @return
     */
    @KeepFromProguard
    static ConnectionWrap __openConnection(String source, boolean openNew) throws SQLException
    {
        ConnectionWrap connection = (ConnectionWrap) TransactionDBHandle.__getConnection__(source);
        if (connection != null && connection.isClosed())
        {
            connection = null;
        }
        if (connection != null || !openNew)
        {
            return connection;
        }
        return __openNewConnection__(source);
    }

    static SqlSession __getSqlSession__(String source)
    {
        MybatisConfig.MOption mOption = getMOption(source);
        MSqlSessionFactoryBuilder sqlSessionFactoryBuilder = mOption.mSqlSessionFactoryBuilder;
        SqlSession sqlSession = sqlSessionFactoryBuilder.getFactory().openSession(true);
        return sqlSession;
    }

    static ConnectionWrap __openNewConnection__(String source)
    {
        MybatisConfig.MOption mOption = getMOption(source);
        MSqlSessionFactoryBuilder builder = mOption.mSqlSessionFactoryBuilder;
        SqlSessionFactory sqlSessionFactory = builder.getFactory();

        MyBatisOption.IConnectionBridge iConnectionBridge = mOption.iConnectionBridge;
        SqlSession sqlSession;
        Connection bridgeConnection = null;
        if (iConnectionBridge != null)
        {
            bridgeConnection = iConnectionBridge
                    .getConnection(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource());
            bridgeConnection = new ConnectionWrapper(bridgeConnection)
            {

                @Override
                public boolean getAutoCommit() throws SQLException
                {
                    if(isClosed()){
                        return true;
                    }
                    return super.getAutoCommit();
                }

                @Override
                public void setAutoCommit(boolean autoCommit) throws SQLException
                {
                    if (!isClosed())
                    {
                        super.setAutoCommit(autoCommit);
                    }
                }

                @Override
                public void close() throws SQLException
                {
                    //bridge会进行关闭。
                    if (!this.isClosed())
                    {
                        super.close();
                    }
                }
            };
            sqlSession = sqlSessionFactory.openSession(bridgeConnection);
        } else
        {
            sqlSession = sqlSessionFactory.openSession(true);
        }
        ConnectionWrap connection = new ConnectionWrap(builder, sqlSession, iConnectionBridge, bridgeConnection)
        {
            @Override
            public void close() throws SQLException
            {
                TransactionDBHandle.__removeConnection__(source);
                super.close();
            }
        };

        TransactionDBHandle.__setConnection__(source, connection);

        return connection;
    }

}
