package cn.xishan.oftenporter.oftendb.mybatis;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.plugin.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisOption
{
    public String rootDir;

    public boolean autoCommit = true;

    /**
     * 是否检测mapper文件变动,主要用于开发阶段,默认false。
     */
    public boolean checkMapperFileChange = false;

    /**
     * 是否自动注册别名。默认true。
     */
    public boolean autoRegisterAlias = true;

    /**
     * 资源根目录，主要用于开发阶段。
     */
    public String resourcesDir;

    /**
     * 用于配置数据源:其中dsType为{@linkplain javax.sql.DataSource}的实现类。
     * <pre>
     *     1.会忽略以"--ignore"结尾的属性。
     * </pre>
     */
    public JSONObject dataSource;

    public List<Interceptor> interceptors;

    public MyBatisOption(String rootDir)
    {
        if (!rootDir.endsWith("/"))
        {
            rootDir += "/";
        }
        this.rootDir = rootDir;
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
