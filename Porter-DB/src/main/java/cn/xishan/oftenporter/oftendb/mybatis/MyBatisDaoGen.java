package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.slf4j.Logger;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoGen implements AutoSetGen
{

    @AutoSet
    MybatisConfig mybatisConfig;
    @AutoSet
    Logger LOGGER;

    Map<String, String> methodMap;


    @PortIn.PortStart(order = 100100)
    public void onStart() throws Exception
    {
        if (mybatisConfig.myBatisOption.javaFuns != null)
        {
            methodMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : mybatisConfig.myBatisOption.javaFuns.entrySet())
            {
                methodMap.put(entry.getKey(), entry.getValue().getName());
            }
        }
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

        if (!myBatis.entityClass.equals(MyBatisMapper.class))
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

    String replaceParams(_MyBatis myBatis, String xml) throws Exception
    {
        xml = myBatis.replaceSqlParams(xml, mybatisConfig.myBatisOption);
        if (methodMap != null && methodMap.size() > 0)
        {

            StringBuilder stringBuilder = new StringBuilder();
            String key = "${java::";
            while (true)
            {
                int index = xml.indexOf(key);
                if (index == -1)
                {
                    break;
                }

                int index2 = xml.indexOf(".", index + key.length());

                if (index2 == -1)
                {
                    throw new RuntimeException("illegal xml file:" + xml.substring(index));
                }

                String name = xml.substring(index + key.length(), index2);
                String className = methodMap.get(name);
                if (className == null)
                {
                    throw new RuntimeException("not found java fun 'java::" + name + "'");
                }
                stringBuilder.append(xml.substring(0, index + 2));
                stringBuilder.append("@").append(className).append("@");
                xml = xml.substring(index2 + 1);
            }
            stringBuilder.append(xml);
            xml = stringBuilder.toString();
        }
        return xml;
    }

    void loadXml(_MyBatis myBatis, String path, File optionMapperFile) throws IOException
    {
        try
        {

            SqlSessionFactory sqlSessionFactory = mybatisConfig.mSqlSessionFactoryBuilder.getFactory();

            Configuration configuration = sqlSessionFactory.getConfiguration();

            byte[] xmlData = null;

            if (optionMapperFile != null)
            {
                if (myBatis.type == MyBatisMapper.Type.RESOURCES)
                {
                    path = getFileRelativePath(myBatis, path);
                }
                ErrorContext.instance().resource(optionMapperFile.getAbsolutePath());
                xmlData = FileTool.getData(new FileInputStream(optionMapperFile), 2048);
            } else if (myBatis.type == MyBatisMapper.Type.RESOURCES)
            {
                path = getFileRelativePath(myBatis, path);
                ErrorContext.instance().resource(path);
                InputStream inputStream = Resources.getResourceAsStream(path);
                xmlData = FileTool.getData(inputStream, 2048);
            } else
            {
                //URL
                ErrorContext.instance().resource(path);
                InputStream inputStream = Resources.getUrlAsStream(path);
                xmlData = FileTool.getData(inputStream, 2048);
            }

            {
//                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//                Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlData));
                String encoding = null;

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(xmlData)));
                String line = bufferedReader.readLine();
                bufferedReader.close();
                if (line != null)
                {//读取第一行的xml编码方式。
                    Pattern encodingPattern = Pattern
                            .compile("encoding([\\s]*)=([\\s]*)(['\"])([^\\r\\n'\"]+)(['\"])",
                                    Pattern.CASE_INSENSITIVE);
                    Matcher matcher = encodingPattern.matcher(line);
                    if (matcher.find())
                    {
                        encoding = matcher.group(4);
                    }
                }

                if (encoding == null)
                {
                    encoding = "utf-8";
                }
                String xml = new String(xmlData, encoding);
                xml = replaceParams(myBatis, xml);
                xmlData = xml.getBytes(Charset.forName(encoding));
            }


            XMLMapperBuilder mapperParser = new XMLMapperBuilder(new ByteArrayInputStream(xmlData), configuration,
                    path, configuration.getSqlFragments());
            mapperParser.parse();
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
        } finally
        {
            ErrorContext.instance().reset();
        }

    }


    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option) throws Exception
    {
        MyBatisField myBatisField = field.getAnnotation(MyBatisField.class);

        if (myBatisField == null)
        {
            LOGGER.debug("the field [{}] not annotated with @[{}]", field, MyBatisField.class.getName());
            MyBatisDaoImpl myBatisDao = new MyBatisDaoImpl(this);
            return myBatisDao;
        }
        _MyBatisField _myBatisField = new _MyBatisField();
        _myBatisField.value = myBatisField.value();
        return genObject(_myBatisField);
    }


    MyBatisDaoImpl genObject(_MyBatisField myBatisField)
    {

        Class<?> mapperClass = myBatisField.value;

        String dir = "";
        String name = mapperClass.getSimpleName() + ".xml";

        MyBatisMapper.Type theType = MyBatisMapper.Type.RESOURCES;
        MyBatis myBatis1;
        MyBatisMapper myBatis2;
        String[] params = null;
        {
            myBatis1 = AnnoUtil.getAnnotation(mapperClass, MyBatis.class);
            if (myBatis1 != null)
            {
                theType = myBatis1
                        .type() == MyBatis.Type.RESOURCES ? MyBatisMapper.Type.RESOURCES : MyBatisMapper.Type.URL;
                if (!myBatis1.dir().equals(""))
                {
                    dir = myBatis1.dir();
                }
                if (!myBatis1.name().equals(""))
                {
                    name = myBatis1.name();
                }
                params = myBatis1.params();
            }
            if (!dir.equals("") && !dir.endsWith("/"))
            {
                dir += "/";
            }
        }

        {
            myBatis2 = AnnoUtil.getAnnotation(mapperClass, MyBatisMapper.class);
            if (myBatis2 != null)
            {
                theType = myBatis2.type();
                if (!myBatis2.dir().equals(""))
                {
                    dir = myBatis2.dir();
                }
                if (!myBatis2.name().equals(""))
                {
                    name = myBatis2.name();
                }
                params = myBatis2.params();
            }
            if (!dir.equals("") && !dir.endsWith("/"))
            {
                dir += "/";
            }
        }


        _MyBatis myBatis = new _MyBatis(theType, dir, name);
        myBatis.daoClass = mapperClass;

        Class<?> entityClass = null;

        if (myBatis2 != null)
        {
            myBatis.isAutoAlias = mybatisConfig.myBatisOption.autoRegisterAlias;
            myBatis.daoAlias = myBatis2.daoAlias();
            myBatis.entityAlias = myBatis2.entityAlias();
            myBatis.entityClass = myBatis2.entityClass();
            entityClass = myBatis.entityClass;
        } else if (myBatis1 != null)
        {
            myBatis.isAutoAlias = mybatisConfig.myBatisOption.autoRegisterAlias;
            myBatis.daoAlias = myBatis1.daoAlias();
            myBatis.entityAlias = myBatis1.entityAlias();
            myBatis.entityClass = myBatis1.entityClass().equals(MyBatis.class) ? MyBatisMapper.class : myBatis1
                    .entityClass();
            entityClass = myBatis.entityClass;
        } else
        {
            myBatis.isAutoAlias = false;
            myBatis.daoAlias = "";
            myBatis.entityAlias = "";
            myBatis.entityClass = MyBatisMapper.class;
        }

        myBatis.init(params);

        String path = dir + name;
        LOGGER.debug("mapper={},type={},entity={},dao={}", path, theType, entityClass, mapperClass);

        MyBatisDaoImpl myBatisDao = new MyBatisDaoImpl(this, myBatis, path);
        if (mybatisConfig.myBatisOption.resourcesDir != null && theType == MyBatisMapper.Type.RESOURCES)
        {
            File file = new File(mybatisConfig.myBatisOption.resourcesDir + getFileRelativePath(myBatis, path));
            if (file.exists() && file.isFile())
            {
                myBatisDao.setMapperFile(file);
            }
        }
        mybatisConfig.mSqlSessionFactoryBuilder.addListener(myBatisDao);
        return myBatisDao;
    }
}
