package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.annotation.PortStart;

/**
 * @author Created by https://github.com/CLovinr on 2020/10/27.
 */
public interface __MyBatisDaoProxy__
{
    @PortStart
    default void onStart() throws Exception
    {
        MyBatisDaoImpl myBatisDao = (MyBatisDaoImpl) MyBatisDao.getMyBatisDao(this);
        myBatisDao.onBindAlias();
        myBatisDao.onParse();
    }
}
