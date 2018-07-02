package cn.xishan.oftenporter.oftendb.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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

//    private void checkMapperClass()
//    {
//        if (myBatis == null)
//        {
//            throw new NullPointerException("there is no mapper dao class");
//        }
//    }

    public MyBatisDaoImpl(MyBatisDaoGen myBatisDaoGen)
    {
        this.myBatisDaoGen = myBatisDaoGen;
    }


    void setMapperFile(File mapperFile)
    {
        this.mapperFile = mapperFile;
    }


    @Override
    public SqlSession getNewSqlSession()
    {
        ConnectionWrap connection = MyBatisBridge.__openSession__(myBatisDaoGen.source, false);
        return connection.getSqlSession();
    }

//    private ConnectionWrap getConnectionWrap()
//    {
//        ConnectionWrap connection = MyBatisBridge.__openSession(myBatisDaoGen.source);
//        return connection;
//    }


//    @Override
//    public <T> T mapper(WObject wObject)
//    {
//        checkMapperClass();
//        T t;
//        ConnectionWrap connectionWrap = getConnectionWrap();
//        t = connectionWrap.getSqlSession().getMapper((Class<T>) myBatis.daoClass);
//        t = doProxy(t, connectionWrap, myBatis.daoClass);
//        return t;
//    }

//    @Override
//    public <T> T mapper(WObject wObject, Class<T> clazz)
//    {
//        T t;
//        ConnectionWrap connectionWrap = getConnectionWrap();
//        t = mapperOther(connectionWrap.getSqlSession(), clazz);
//        t = doProxy(t, connectionWrap, clazz);
//        return t;
//    }


//    private final <T> T doProxy(T t, ConnectionWrap connectionWrap, Class<?> type)
//    {
//        //代理后可支持重新加载mybatis文件
//        Object proxyT = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{
//                        type, _MyBatisDaoProxy.class},
//                (proxy, method, args) -> {
//                    if (!Modifier.isInterface(method.getDeclaringClass().getModifiers()))
//                    {
//                        return method.invoke(t, args);
//                    }
//                    Object rs = method.invoke(t, args);
//                    if (connectionWrap.getAutoCommit())
//                    {
//                        TransactionJDBCHandle.__removeConnection__(myBatisDaoGen.source);
//                        connectionWrap.close();
//                    }
//                    return rs;
//                });
//        return (T) proxyT;
//    }

    @Override
    public <T> T mapper(Class<T> clazz)
    {
        Object dao = MyBatisDaoGen.doProxy(this, clazz, myBatisDaoGen.source);
        return (T) dao;
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
