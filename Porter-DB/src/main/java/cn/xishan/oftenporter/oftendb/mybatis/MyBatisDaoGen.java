package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionJDBCHandle;
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
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class MyBatisDaoGen implements AutoSetGen
{

    String source;

    @AutoSet
    Logger LOGGER;


    @PortIn.PortStart(order = 100100)
    public void onStart()
    {
        MyBatisBridge.start();
    }

    MybatisConfig.MOption moption()
    {
        return MyBatisBridge.getMOption(source);
    }

    @PortIn.PortDestroy
    public void onDestroy()
    {
        MyBatisBridge.destroy();
    }

    void bindAlias(_MyBatis myBatis)
    {
        SqlSessionFactory sqlSessionFactory = moption().mSqlSessionFactoryBuilder.getFactory();
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
        path = PackageUtil.getPathWithRelative('/', moption().myBatisOption.rootDir, path, "/");
        return path;
    }

    String replaceParams(_MyBatis myBatis, String xml) throws Exception
    {
        xml = myBatis.replaceSqlParams(xml);
        Map<String, String> methodMap = moption().mSqlSessionFactoryBuilder.methodMap;
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

            SqlSessionFactory sqlSessionFactory = moption().mSqlSessionFactoryBuilder.getFactory();

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
                        new InputStreamReader(new ByteArrayInputStream(xmlData), Charset.defaultCharset()));
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
        MyBatisField myBatisField = AnnoUtil.getAnnotation(field, MyBatisField.class);

        MyBatisDaoImpl myBatisDao;
        if (myBatisField == null || Object.class.equals(myBatisField.value()))
        {
            LOGGER.debug("the field [{}] not annotated with @[{}]", field, MyBatisField.class.getName());
            this.source = myBatisField != null ? myBatisField.source() : MyBatisOption.DEFAULT_SOURCE;
            myBatisDao = new MyBatisDaoImpl(this);
        } else
        {
            this.source = myBatisField.source();
            _MyBatisField _myBatisField = new _MyBatisField();
            _myBatisField.value = myBatisField.value();
            myBatisDao = genObject(_myBatisField);
        }
        Class fieldType = field.getType();
        if (!fieldType.equals(MyBatisDao.class))
        {
            //代理
            if (!Modifier.isInterface(fieldType.getModifiers()))
            {
                throw new RuntimeException("just support interface,but given " + field);
            }
            Object proxyObject = Proxy.newProxyInstance(fieldType.getClassLoader(), new Class[]{
                    fieldType
            }, (proxy, method, args) -> {
                if(!Modifier.isInterface(method.getDeclaringClass().getModifiers())){
                    return method.invoke(myBatisDao,args);
                }
                ConnectionImpl mConnectionImpl = MyBatisBridge.__openSession(source);
                Object dao = myBatisDao._mapper(mConnectionImpl.getSqlSession(), fieldType);
                Object rs = method.invoke(dao, args);
                Connection connection = mConnectionImpl.getConnection();
                if (connection.getAutoCommit())
                {
                    TransactionJDBCHandle.__removeConnection__(source);
                    connection.close();
                }
                return rs;
            });
            return proxyObject;
        }
        return myBatisDao;
    }


    MyBatisDaoImpl genObject(_MyBatisField myBatisField)
    {

        Class<?> mapperClass = myBatisField.value;

        String dir = "";
        String name = mapperClass.getSimpleName() + ".xml";

        MyBatisMapper.Type theType = MyBatisMapper.Type.RESOURCES;
        MyBatisMapper myBatisMapper;
        String[] params = null;


        {
            myBatisMapper = AnnoUtil.getAnnotation(mapperClass, MyBatisMapper.class);
            if (myBatisMapper != null)
            {
                theType = myBatisMapper.type();
                if (!myBatisMapper.dir().equals(""))
                {
                    dir = myBatisMapper.dir();
                }
                if (!myBatisMapper.name().equals(""))
                {
                    name = myBatisMapper.name();
                }
                params = myBatisMapper.params();
            }
            if (!dir.equals("") && !dir.endsWith("/"))
            {
                dir += "/";
            }
        }


        _MyBatis myBatis = new _MyBatis(theType, moption().myBatisOption.resourcesDir, name);
        myBatis.daoClass = mapperClass;

        Class<?> entityClass = null;

        if (myBatisMapper != null)
        {
            myBatis.isAutoAlias = moption().myBatisOption.autoRegisterAlias;
            myBatis.daoAlias = myBatisMapper.daoAlias();
            myBatis.entityAlias = myBatisMapper.entityAlias();
            myBatis.entityClass = myBatisMapper.entityClass();
            entityClass = myBatis.entityClass;
        } else
        {
            myBatis.isAutoAlias = false;
            myBatis.daoAlias = "";
            myBatis.entityAlias = "";
            myBatis.entityClass = MyBatisMapper.class;
        }

        String path = dir + name;
        myBatis.setPath(getFileRelativePath(myBatis,path));
        myBatis.init(params);

        LOGGER.debug("mapper={},type={},entity={},dao={}", path, theType, entityClass, mapperClass);

        MyBatisDaoImpl myBatisDao = new MyBatisDaoImpl(this, myBatis, path);
        if (moption().myBatisOption.resourcesDir != null && theType == MyBatisMapper.Type.RESOURCES)
        {
            File file = new File(moption().myBatisOption.resourcesDir + getFileRelativePath(myBatis, path));
            if (file.exists() && file.isFile())
            {
                myBatisDao.setMapperFile(file);
            }
        }
        moption().mSqlSessionFactoryBuilder.addListener(myBatisDao);
        return myBatisDao;
    }
}
