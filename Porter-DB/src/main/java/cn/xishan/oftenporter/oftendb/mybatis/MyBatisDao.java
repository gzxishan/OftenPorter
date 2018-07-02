package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import org.apache.ibatis.session.SqlSession;


/**
 * <pre>
 *     1.应该加上@{@linkplain MyBatisField}注解，从而使得对应的DAO被绑定。
 *     2.对应的接口建议加上{@linkplain MyBatisMapper}注解,在该注解上可以设置dao别名和实体别名。
 * </pre>
 *
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

}
