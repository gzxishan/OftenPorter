package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/6.
 */
class MybatisConfig
{
    MyBatisOption myBatisOption;
    MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder;

    //确保至少有一个Dao，从而会进行初始化
    @AutoSet
    MyBatisDao myBatisDao;

    public MybatisConfig(MyBatisOption myBatisOption,
            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder)
    {
        this.myBatisOption = myBatisOption;
        this.mSqlSessionFactoryBuilder = mSqlSessionFactoryBuilder;
    }
}
