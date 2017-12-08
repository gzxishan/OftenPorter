package cn.xishan.oftenporter.oftendb.mybatis;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/6.
 */
class MybatisConfig
{
    MyBatisOption myBatisOption;
    MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder;

    public MybatisConfig(MyBatisOption myBatisOption,
            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder)
    {
        this.myBatisOption = myBatisOption;
        this.mSqlSessionFactoryBuilder = mSqlSessionFactoryBuilder;
    }
}
