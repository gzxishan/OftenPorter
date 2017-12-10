package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoGen implements AutoSetGen
{

    @AutoSet
    MybatisConfig mybatisConfig;


    @AutoSet
    Logger LOGGER;


    @PortIn.PortStart(order = 100100)
    public void onStart() throws Exception
    {
        mybatisConfig.mSqlSessionFactoryBuilder.onStart();
    }

    @PortIn.PortDestroy
    public void onDestroy()
    {
        mybatisConfig.mSqlSessionFactoryBuilder.onDestroy();
    }

    void bindAlias(_MyBatis myBatis)
    {
        SqlSessionFactory sqlSessionFactory = mybatisConfig.mSqlSessionFactoryBuilder.getFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();

        if (WPTool.notNullAndEmpty(myBatis.daoAlias))
        {
            typeAliasRegistry.registerAlias(myBatis.daoAlias, myBatis.daoClass);
        } else if (myBatis.isAutoAlias)
        {
            typeAliasRegistry.registerAlias(myBatis.daoClass);
        }

        if (!myBatis.entityClass.equals(MyBatis.class))
        {
            if (WPTool.notNullAndEmpty(myBatis.entityAlias))
            {
                typeAliasRegistry.registerAlias(myBatis.entityAlias, myBatis.entityClass);
            } else if (myBatis.isAutoAlias)
            {
                typeAliasRegistry.registerAlias(myBatis.entityClass);
            }
        }

    }

    private String getFileRelativePath(_MyBatis myBatis, String path)
    {
        path = PackageUtil.getPathWithRelative('/', mybatisConfig.myBatisOption.rootDir, path, "/");
        return path;
    }

    String loadXml(_MyBatis myBatis, String path, File optionMapperFile) throws IOException
    {
        try
        {

            SqlSessionFactory sqlSessionFactory = mybatisConfig.mSqlSessionFactoryBuilder.getFactory();

            Configuration configuration = sqlSessionFactory.getConfiguration();

            if (optionMapperFile != null)
            {
                if (myBatis.type == MyBatis.Type.RESOURCES)
                {
                    path = getFileRelativePath(myBatis, path);
                }
                ErrorContext.instance().resource(optionMapperFile.getAbsolutePath());
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(new FileInputStream(optionMapperFile),
                        configuration,
                        path,
                        configuration.getSqlFragments());
                mapperParser.parse();
            } else if (myBatis.type == MyBatis.Type.RESOURCES)
            {
                path = getFileRelativePath(myBatis, path);
                ErrorContext.instance().resource(path);
                InputStream inputStream = Resources.getResourceAsStream(path);
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, path,
                        configuration.getSqlFragments());
                mapperParser.parse();
            } else
            {
                //URL
                ErrorContext.instance().resource(path);
                InputStream inputStream = Resources.getUrlAsStream(path);
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, path,
                        configuration.getSqlFragments());
                mapperParser.parse();
            }
        } finally
        {
            ErrorContext.instance().reset();
        }

        return path;
    }


    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option) throws Exception
    {


        MyBatisField myBatisField = field.getAnnotation(MyBatisField.class);

        if (myBatisField == null)
        {
            LOGGER.debug("the field {} not annotated with@{}", field, MyBatisField.class.getName());
            MyBatisDaoImpl myBatisDao = new MyBatisDaoImpl(this);
            return myBatisDao;
        }

        Class<?> mapperClass = myBatisField.value();

        String dir = "";
        String name = mapperClass.getSimpleName() + ".xml";

        MyBatis _myBatis = AnnoUtil.getAnnotation(mapperClass, MyBatis.class);

        MyBatis.Type type = MyBatis.Type.RESOURCES;

        if (_myBatis != null)
        {
            type = _myBatis.type();
            if (!_myBatis.dir().equals(""))
            {
                dir = _myBatis.dir();
            }
            if (!_myBatis.name().equals(""))
            {
                name = _myBatis.name();
            }
        }


        if (!dir.equals("") && !dir.endsWith("/"))
        {
            dir += "/";
        }

        _MyBatis myBatis = new _MyBatis(type, dir, name);
        myBatis.daoClass = mapperClass;
        if (myBatis == null)
        {
            myBatis.isAutoAlias = false;
            myBatis.daoAlias = "";
            myBatis.entityAlias = "";
            myBatis.entityClass = MyBatis.class;
        } else
        {
            myBatis.isAutoAlias = mybatisConfig.myBatisOption.autoRegisterAlias;
            myBatis.daoAlias = _myBatis.daoAlias();
            myBatis.entityAlias = _myBatis.entityAlias();
            myBatis.entityClass = _myBatis.entityClass();
        }


        String path = dir + name;
        LOGGER.debug("mapper={},type={}", path, type);

        MyBatisDaoImpl myBatisDao = new MyBatisDaoImpl(this, myBatis, path);
        if (mybatisConfig.myBatisOption.resourcesDir != null && type == MyBatis.Type.RESOURCES)
        {
            File file = new File(mybatisConfig.myBatisOption.resourcesDir + getFileRelativePath(myBatis, path));
            myBatisDao.setMapperFile(file);
        }
        mybatisConfig.mSqlSessionFactoryBuilder.addListener(myBatisDao);
        return myBatisDao;
    }
}
