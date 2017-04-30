package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBHandleSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import com.alibaba.fastjson.JSONObject;

/**
 * <pre>
 *     通过js文件来生成sql语句，也可以执行并返回结果。
 *     一、通过添加context范围({@linkplain PorterConf#addContextAutoSet(Class, Object) addContextAutoSet(Class, Object)})的注解参数{@linkplain JDaoOption JDaoOption}来进行相关配置：
 *     二、js文件的格式：
 *     1.一个对象一个文件，每个文件是隔离的。
 *     2.内置变量jdaoBridge：
 *        1）tableNamePrefix：表名前缀。
 *        2）sqlArgs(sql,args)：生成{@linkplain JSqlArgs JSqlArgs}对象
 *        3）sqlExecutor(sql,args)：生成{@linkplain AdvancedExecutor AdvancedExecutor}对象
 *        4）sqlQuery(sql,args)：生成{@linkplain AdvancedQuery AdvancedQuery}对象
 *     3.js执行数据库查询的函数格式:function(connection,json)
 *    <strong>注意：</strong>对应的{@linkplain DBHandleSource DBHandleSource}必须实现{@linkplain SqlSource SqlSource}接口。
 * </pre>
 * Created by chenyg on 2017-04-29.
 */
@AutoSet.AutoSetDefaultDealt(gen = JDaoGen.class)
public interface JDao
{
    /**
     * 将获取调用处所在的方法名。
     *
     * @param json
     * @param <T>
     * @return
     */
    <T> T query(JSONObject json, WObject wObject);

    <T> T query(String method, JSONObject json, WObject wObject);

    /**
     * 将获取调用处所在的方法名。
     *
     * @param json
     * @param <T>
     * @return
     */
    <T> T execute(JSONObject json, WObject wObject);

    <T> T execute(String method, JSONObject json, WObject wObject);
}
