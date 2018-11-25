package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@AutoSetDefaultDealt(gen = MyBatisDaoGen.class)
public interface MyBatisDao
{

    /**
     * 会获取新的sql会话,记得手动关闭。
     *
     * @return
     */
    SqlSession getNewSqlSession();

    /**
     * 会获取新的sql会话。
     *
     * @return
     */
    <T> T mapper(Class<T> clazz);

    /**
     * 如果开启了事务，则返回当前连接；否则返回新的连接对象。
     *
     * @return
     */
    Connection currentConnection();

    /**
     * 重新加载mybatis。
     */
    void reloadMybatis() throws Throwable;

}
