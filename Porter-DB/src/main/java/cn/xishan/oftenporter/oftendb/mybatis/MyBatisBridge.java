package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionJDBCHandle;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisBridge
{

    private static MybatisConfig mybatisConfig;

    static
    {
        AnnoUtil.Advanced.addWhite(IDynamicAnnotationImprovableForDao.class.getName());
    }

    static void start()
    {
        if (mybatisConfig == null)
        {
            throw new InitException("not init!");
        }
        mybatisConfig.start();
    }

    static void destroy()
    {
        mybatisConfig.start();
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
            DataSource dataSource = (DataSource) WPTool.newObject(clazz);
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
            mybatisConfig.put(myBatisOption, mSqlSessionFactoryBuilder);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @KeepFromProguard
    static ConnectionWrap __openSession(String source)
    {
        ConnectionWrap connection = (ConnectionWrap) TransactionJDBCHandle.__getConnection__(source);
        if (connection != null)
        {
            return connection;
        }
        return __openSession__(source, true);
    }

    static ConnectionWrap __openSession__(String source, boolean set2Handle)
    {
        MybatisConfig.MOption mOption = getMOption(source);
        MSqlSessionFactoryBuilder sqlSessionFactoryBuilder = mOption.mSqlSessionFactoryBuilder;
        SqlSession sqlSession = sqlSessionFactoryBuilder.getFactory().openSession(true);
        ConnectionWrap connection = new ConnectionWrap(sqlSession);
        if (set2Handle)
        {
            TransactionJDBCHandle.__setConnection__(source, connection);
        }
        return connection;
    }

}
