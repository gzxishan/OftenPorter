package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/10.
 */
class _MyBatis
{
    MyBatis.Type type;
    String dir;
    String name;
    Class<?> daoClass;
    String daoAlias;
    String entityAlias;
    Class<?> entityClass;

    boolean isAutoAlias;

    public _MyBatis(MyBatis.Type type, String dir, String name)
    {
        this.type = type;
        this.dir = dir;
        this.name = name;
    }
}
