package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisField;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     1.应该加上@{@linkplain MyBatisField}注解，从而使得对应的DAO被绑定。
 *     2.对应的接口可加上{@linkplain MyBatis}注解,在该注解上可以设置dao别名和实体别名。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@AutoSetDefaultDealt(gen = MyBatisDaoGen.class)
public interface MyBatisDao
{

    /**
     * 会获取新的sql会话。
     *
     * @return
     */
    SqlSession getSqlSession();

    /**
     * 会获取新的sql会话。
     *
     * @return
     */
    Connection getConnection();

    Connection getConnection(WObject wObject);

    /**
     * 会获取新的sql会话。
     *
     * @return
     */
    <T> T getMapper(Class<T> clazz);

    SqlSession getSqlSession(WObject wObject);

    <T> T mapper(WObject wObject);

    <T> T mapper(WObject wObject, Class<T> clazz);

    <T> T selectOne(WObject wObject, String statement);


    <T> T selectOne(WObject wObject, String statement, Object parameter);


    <E> List<E> selectList(WObject wObject, String statement);


    <E> List<E> selectList(WObject wObject, String statement, Object parameter);


    <E> List<E> selectList(WObject wObject, String statement, Object parameter, RowBounds rowBounds);


    <K, V> Map<K, V> selectMap(WObject wObject, String statement, String mapKey);


    <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey);

    <K, V> Map<K, V> selectMap(WObject wObject, String statement, Object parameter, String mapKey, RowBounds rowBounds);

    <T> Cursor<T> selectCursor(WObject wObject, String statement);

    <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter);

    <T> Cursor<T> selectCursor(WObject wObject, String statement, Object parameter, RowBounds rowBounds);

    void select(WObject wObject, String statement, Object parameter, ResultHandler handler);

    void select(WObject wObject, String statement, ResultHandler handler);

    void select(WObject wObject, String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

    int insert(WObject wObject, String statement);

    int insert(WObject wObject, String statement, Object parameter);

    int update(WObject wObject, String statement);

    int update(WObject wObject, String statement, Object parameter);


    int delete(WObject wObject, String statement);

    int delete(WObject wObject, String statement, Object parameter);


}
