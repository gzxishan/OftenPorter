package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.plugin.Interceptor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisOption
{

    public static final String DEFAULT_SOURCE = "default";

    /**
     * 指定数据源类型，见{@linkplain MyBatisField#source()}
     */
    public String source = DEFAULT_SOURCE;
    /**
     * 在资源目录下的子目录
     */
    public String rootDir;

    public boolean autoCommit = true;

    /**
     * 是否允许mapper中的id覆盖，默认true。
     */
    public boolean enableMapperOverride = true;

    /**
     * 是否检测mapper文件变动,主要用于开发阶段,默认false。
     */
    public boolean checkMapperFileChange = false;

    /**
     * 是否自动注册别名。默认true。
     */
    public boolean autoRegisterAlias = true;

    /**
     * 资源目录所对应的系统文件，主要用于开发阶段。
     */
    public String resourcesDir;

    /**
     * 用于配置数据源:其中dsType为{@linkplain javax.sql.DataSource}的实现类。
     * <pre>
     *     1.会忽略以"--ignore"结尾的属性。
     * </pre>
     */
    public JSONObject dataSource;

    /**
     * 优先级高于{@linkplain #dataSource}
     */
    public DataSource dataSourceObject;

    public List<Interceptor> interceptors;

    /**
     * 用于添加静态函数到mybatis中。通过${java::keyName.staticFunName(args)}调用,且只支持在mapper文件中调用。
     */
    public Map<String, Class<?>> javaFuns;

    public MyBatisOption(String rootDir)
    {
        if (!rootDir.endsWith("/"))
        {
            rootDir += "/";
        }
        this.rootDir = rootDir;
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
}
