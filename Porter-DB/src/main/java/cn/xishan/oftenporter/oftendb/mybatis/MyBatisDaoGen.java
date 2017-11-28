package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoGen implements AutoSetGen
{

    @AutoSet
    MyBatisOption myBatisOption;

    @AutoSet
    SqlSessionFactory sqlSessionFactory;

    @AutoSet
    Logger LOGGER;


    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option) throws Exception
    {



        MyBatisField myBatisField = field.getAnnotation(MyBatisField.class);

        if (myBatisField == null)
        {
            throw new NullPointerException(
                    "the field " + field + " not annotated with @" + MyBatisField.class.getName());
        }

        Class<?> mapperClass = myBatisField.value();

        String dir = "";
        String name = mapperClass.getSimpleName() + ".xml";

        MyBatis _myBatis = AnnoUtil.getAnnotation(mapperClass, MyBatis.class);

        MyBatis.Type type = MyBatis.Type.RESOURCES;

        if(_myBatis!=null){
            type=_myBatis.type();
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
        String path = dir + name;
        LOGGER.debug("mapper={},type={}", path, type);
        Configuration configuration = sqlSessionFactory.getConfiguration();
        if (type == MyBatis.Type.RESOURCES)
        {
            path = PackageUtil.getPathWithRelative('/', myBatisOption.rootDir, path, "/");
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

        MyBatisDao myBatisDao = new MyBatisDaoImpl(mapperClass);

        return myBatisDao;
    }
}
