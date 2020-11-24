package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisAlias;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.plugin.Interceptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * mybatis对应的Mapper接口上必须加上@{@linkplain MyBatisMapper}注解
 *
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisOption implements Cloneable
{

    public interface IMybatisStateListener
    {
        void onStart();

        void onDestroy();

        void beforeReload();

        void afterReload();

        default void beforeFirstLoad()
        {

        }

        default void afterFirstLoad()
        {

        }

        void onReloadFailed(Throwable throwable);
    }

    /**
     * 用于与其他框架集成。
     */
    public interface IConnectionBridge
    {
        /**
         * 获取数据库连接，通过该方式使得其他框架的事务管理能正常发挥作用。
         *
         * @param dataSource
         * @return
         */
        Connection getConnection(DataSource dataSource);

        void closeConnection(DataSource dataSource, Connection connection);

        DataSource onDataSourceChanged(@MayNull DataSource last, DataSource dataSource);
    }

    public static final String DEFAULT_SOURCE = "default";

    /**
     * 指定数据源类型，见{@linkplain MyBatisField#source()}
     */
    public String source = DEFAULT_SOURCE;
    /**
     * 在资源目录下的子目录
     */
    public Set<String> rootDirSet;

    /**
     * 是否允许mapper中的id覆盖，默认true。
     */
    public boolean enableMapperOverride = true;

    /**
     * 是否用{@linkplain OftenCallException}包装所有dao抛出的异常,默认为false。
     */
    public boolean wrapDaoThrowable = false;

    /**
     * 是否检测mapper文件变动,主要用于开发阶段,默认false。
     */
    public boolean checkMapperFileChange = false;


    /**
     * 设置字段被包裹的内容，默认为"`"。
     */
    public String columnCoverString = "`";

    public IMybatisStateListener mybatisStateListener;

    /**
     * 是否自动注册别名,对{@linkplain MyBatisMapper#entityClass()}及被{@linkplain MyBatisMapper}注解过的类，另见{@linkplain MyBatisAlias
     * }。默认true。
     */
    public boolean autoRegisterAlias = true;

    /**
     * 资源目录所对应的系统文件，主要用于开发阶段。
     */
    public String resourcesDir;

    /**
     * 从数据库获取的字段名的大小写如何转换：null表示默认，true表示转换为小写，false表示转换为大写。默认为null。
     */
    public Boolean metaDataTableColumnsToLowercase = null;

    /**
     * 用于配置数据源:其中dsType(或type)为{@linkplain javax.sql.DataSource}的实现类。
     * <pre>
     *     1.会忽略以"--ignore"结尾的属性。
     * </pre>
     */
    public JSONObject dataSource;


    /**
     * 用于配置数据源属性前缀，如“datasource.,datasource.type--ignore”,另见{@linkplain IConfigData},优先级高于低于{@linkplain #dataSource}
     * :其中dsType(或type)
     * 为{@linkplain javax.sql.DataSource}的实现类。
     * <pre>
     *     1.会忽略以"--ignore"结尾的属性。
     * </pre>
     */
    public String dataSourceProperPrefix;

    /**
     * 优先级高于{@linkplain #dataSource}
     */
    public DataSource dataSourceObject;

    public List<Interceptor> interceptors;

    /**
     * 用于添加静态函数到mybatis中。通过@java::keyName.staticFunName(args)调用,且只支持在mapper文件中调用。
     */
    public Map<String, Class<?>> javaFuns;

    public IMapperNameHandle iMapperNameHandle;

    public IConnectionBridge iConnectionBridge;

    public String[] initSqls;

    /**
     * 初始化失败时，是否抛出有异常。
     */
    public boolean throwOnInitError = false;

    /**
     * @param rootDir               资源目录,另见{@linkplain #addMoreRootDirs(String...)}
     * @param checkMapperFileChange 是否监听mapper文件变化，另见{@linkplain #mybatisStateListener}
     */
    public MyBatisOption(@MayNull IConnectionBridge iConnectionBridge, String rootDir, boolean checkMapperFileChange)
    {
        this.iConnectionBridge = iConnectionBridge;
        this.rootDirSet = new HashSet<>();
        addMoreRootDirs(rootDir);
        this.checkMapperFileChange = checkMapperFileChange;
    }

    /**
     * @param checkMapperFileChange 是否监听mapper文件变化，另见{@linkplain #mybatisStateListener}
     * @param rootDirs              资源目录,另见{@linkplain #addMoreRootDirs(String...)}
     */
    public MyBatisOption(@MayNull IConnectionBridge iConnectionBridge, boolean checkMapperFileChange,
            String... rootDirs)
    {
        this.iConnectionBridge = iConnectionBridge;
        this.rootDirSet = new HashSet<>();
        addMoreRootDirs(rootDirs);
        this.checkMapperFileChange = checkMapperFileChange;
    }

    /**
     * 增加更多的资源子目录(相对于java资源目录下的子目录，以/开头。)
     *
     * @param rootDirs
     */
    public void addMoreRootDirs(String... rootDirs)
    {
        for (String rootDir : rootDirs)
        {
            if (OftenTool.isEmpty(rootDir))
            {
                continue;
            }
            if (!rootDir.endsWith("/"))
            {
                rootDir += "/";
            }
            if (!rootDir.startsWith("/"))
            {
                rootDir = "/" + rootDir;
            }
            rootDirSet.add(rootDir);
        }
    }

    public void addJavaFuns(String name, Class<?> clazz)
    {
        if (javaFuns == null)
        {
            javaFuns = new HashMap<>();
        }
        javaFuns.put(name, clazz);
    }

    public void addInterceptor(Interceptor interceptor)
    {
        if (interceptors == null)
        {
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
    }

    @Override
    public MyBatisOption clone()
    {
        try
        {
            MyBatisOption myBatisOption = (MyBatisOption) super.clone();
            return myBatisOption;
        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
