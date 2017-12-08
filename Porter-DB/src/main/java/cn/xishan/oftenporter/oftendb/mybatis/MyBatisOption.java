package cn.xishan.oftenporter.oftendb.mybatis;

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

    @Deprecated
    public int checkMapperFileDelaySeconds = 30;

    /**
     * 资源根目录，主要用于开发阶段。
     */
    public String resourcesDir;

    public MyBatisOption(String rootDir)
    {
        this.rootDir = rootDir;
    }
}
