package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisAlias;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionDBHandle;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal._AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.*;
import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.Charset;
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


    public MyBatisDaoGen()
    {
        //LogUtil.printErrPos("....");
    }

    @PortIn.PortStart(order = 100)
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

        if (myBatis.isAutoAlias)
        {
            typeAliasRegistry.registerAlias(myBatis.daoClass);
            LOGGER.debug("auto register alias:type={}", myBatis.daoClass);
        }

        if (!myBatis.entityClass.equals(MyBatisMapper.class))
        {
            if (myBatis.isAutoAlias)
            {
                typeAliasRegistry.registerAlias(myBatis.entityClass);
                LOGGER.debug("auto register alias:type={}", myBatis.entityClass);
            }
        }

        _MyBatis.Alias[] aliases = myBatis.aliases;
        for (_MyBatis.Alias alias : aliases)
        {
            if (WPTool.isEmpty(alias.alias))
            {
                typeAliasRegistry.registerAlias(alias.type);
                LOGGER.debug("register alias:type={}", alias.type);
            } else
            {
                typeAliasRegistry.registerAlias(alias.alias, alias.type);
                LOGGER.debug("register alias:alias={},type={}", alias.alias, alias.type);
            }
        }

    }

    private String getFileRelativePath(_MyBatis myBatis, String path)
    {
        MyBatisOption myBatisOption = moption().myBatisOption;
        for (String rootDir : myBatisOption.rootDirSet)
        {
            String res_path = PackageUtil.getPathWithRelative(rootDir, path);
            URL url = ResourceUtil.getAbsoluteResource(res_path);
            if (url != null)
            {
                return res_path;
            }
        }
//        path = PackageUtil.getPathWithRelative('/', moption().myBatisOption.rootDir, path, "/");
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
                stringBuilder.append(xml, 0, index + 2);
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
                LOGGER.info("load xml:dao={},file={}", myBatis.daoClass, optionMapperFile);
                xmlData = FileTool.getData(new FileInputStream(optionMapperFile), 2048);
            } else if (myBatis.type == MyBatisMapper.Type.RESOURCES)
            {
                path = getFileRelativePath(myBatis, path);
                LOGGER.info("load xml:dao={},path={}", myBatis.daoClass, path);
                URL url = ResourceUtil.getAbsoluteResource(path);
                if (url == null)
                {
                    throw new IOException("not found:" + path);
                }
                InputStream inputStream = url.openStream();
                xmlData = FileTool.getData(inputStream, 2048);
            } else
            {
                //URL
                LOGGER.info("load xml:dao={},url={}", myBatis.daoClass, path);
                URL url = new URL(path);
                InputStream inputStream = url.openStream();
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
        }

    }


    @Override
    public Object genObject(IConfigData iConfigData,Class<?> currentObjectClass, Object currentObject, Field field, Class<?> realFieldType,
            _AutoSet autoSet,
            String option)
    {
        MyBatisField myBatisField = AnnoUtil.getAnnotation(field, MyBatisField.class);
        if (myBatisField == null)
        {
            LOGGER.debug("the field [{}] not annotated with [@{}],use source:{}", field,
                    MyBatisField.class.getSimpleName(), MyBatisOption.DEFAULT_SOURCE);
            this.source = MyBatisOption.DEFAULT_SOURCE;
        } else
        {
            this.source = myBatisField.source();
        }

        Object result;
        if (realFieldType.equals(MyBatisDao.class))
        {
            result = new MyBatisDaoImpl(this);
        } else
        {
            if (!Modifier.isInterface(realFieldType.getModifiers()))
            {
                throw new RuntimeException("just support interface,but given " + field);
            }

            MyBatisDaoImpl myBatisDao;
            _MyBatisField _myBatisField = new _MyBatisField();
            _myBatisField.value = realFieldType;
            myBatisDao = genObject(_myBatisField);
            //代理
            result = doProxy(myBatisDao, realFieldType, source);
        }

        return result;
    }

    interface __MyBatisDaoProxy__
    {

    }


    static Object doProxy(MyBatisDaoImpl myBatisDao, Class<?> type, String source)
    {
        //代理dao后可支持重新加载mybatis文件、支持事务控制等。
        InvocationHandlerWithCommon invocationHandler = new InvocationHandlerWithCommon(myBatisDao)
        {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if (method.equals(TO_STRING_METHOD))
                {
                    return type.getName() + "@@" + myBatisDao.getClass().getSimpleName() + myBatisDao.hashCode();
                }
                return super.invoke(proxy, method, args);
            }

            @Override
            public Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable
            {
                ConnectionWrap connectionWrap = MyBatisBridge.__openSession(source);
                Object dao = myBatisDao.getMapperDao(connectionWrap.getSqlSession(), type);
                Object rs = method.invoke(dao, args);
                if (connectionWrap.getAutoCommit())
                {
                    TransactionDBHandle.__removeConnection__(source);
                    connectionWrap.close();
                } else if (connectionWrap.isBridgeConnection())
                {
                    TransactionDBHandle.__removeConnection__(source);
                }
                return rs;
            }
        };

        Object proxyT = ProxyUtil.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
                type, __MyBatisDaoProxy__.class}, invocationHandler);
        return proxyT;
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
        IMapperNameHandle iMapperNameHandle = moption().myBatisOption.iMapperNameHandle;
        if (iMapperNameHandle != null)
        {
            name = iMapperNameHandle.getMapperName(myBatisField.value, name);
        }


        MyBatisAlias[] myBatisAliases = AnnoUtil.getRepeatableAnnotations(mapperClass, MyBatisAlias.class);
        _MyBatis.Alias[] aliases = new _MyBatis.Alias[myBatisAliases.length];
        for (int i = 0; i < myBatisAliases.length; i++)
        {
            MyBatisAlias myBatisAlias = myBatisAliases[i];
            aliases[i] = new _MyBatis.Alias(myBatisAlias.alias(), myBatisAlias.type());
        }

        _MyBatis myBatis = new _MyBatis(aliases, theType, moption().myBatisOption.resourcesDir, name);
        myBatis.daoClass = mapperClass;

        Class<?> entityClass = null;

        if (myBatisMapper != null)
        {
            myBatis.isAutoAlias = moption().myBatisOption.autoRegisterAlias;
            myBatis.entityClass = myBatisMapper.entityClass();
            entityClass = myBatis.entityClass;
            if (myBatis.entityClass.equals(MyBatisMapper.class) && myBatisMapper
                    .entityClassFromGenericTypeAt() >= 0)
            {
                //获取
                Class<?> realType = myBatisField.value;
                myBatis.entityClass = AnnoUtil.Advance
                        .getDirectGenericRealTypeAt(realType, myBatisMapper.entityClassFromGenericTypeAt());
            }

            if (myBatis.entityClass.equals(MyBatisMapper.class) && !myBatisMapper
                    .entityClassFromGenericTypeBySuperType().equals(MyBatisMapper.class))
            {
                Class<?> realType = myBatisField.value;
                myBatis.entityClass = AnnoUtil.Advance
                        .getDirectGenericRealTypeBySuperType(realType,
                                myBatisMapper.entityClassFromGenericTypeBySuperType());
            }

        } else
        {
            myBatis.isAutoAlias = false;
            myBatis.entityClass = MyBatisMapper.class;
        }

        String path = dir + name;
        myBatis.setPath(getFileRelativePath(myBatis, path));
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
