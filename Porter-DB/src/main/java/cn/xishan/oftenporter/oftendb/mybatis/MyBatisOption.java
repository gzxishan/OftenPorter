package cn.xishan.oftenporter.oftendb.mybatis;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisOption
{
    public String rootDir;

    public boolean autoCommit=true;

    public MyBatisOption(String rootDir)
    {
        this.rootDir = rootDir;
    }
}
