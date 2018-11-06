package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;

import java.lang.annotation.*;

/**
 * <p>
 * 说明：只支持在mapper文件中调用，通过$[key]来引用
 * </p>
 * <ol>
 * <li>
 * &#60;!--$classpath:path[!JsonString]--&#62;:表示导入包内path资源内容,相对于对应的Dao的类路径(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$path:path[!JsonString]--&#62;:表示导入包内path资源内容,相对于当前mapper的路径(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$file:path[!JsonString]--&#62;:表示导入path文件内容(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$json:JsonString--&#62;:表示导入配置,会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$enable:varName--&#62;...&#60;!--$enable-end:varName--&#62;:表示是否启用代码块，当varName为逻辑假时、中间的代码会被移除
 * </li>
 * <li>
 * 判断varName变量为真或假的方式：1）为逻辑假的情况，变量名为空,变量本身为false、空字符串(trim后的)、null、空数组、空集合、空Map、数字0、字符0、字符串0；2）其余情况为真。
 * </li>
 * <li>
 * <strong>默认参数:</strong>
 * <ul>
 * <li>如果设置了{@linkplain MyBatisMapper#entityClass()},$[entity]为当前实体类简单名称(如User)，$[entityClass]为当前实体类名(如cn.xishan.xxx
 * .entity.User)</li>
 * <li>$[mapperDao]为当前接口简单名称，$[mapperDaoClass]为当前接口类名</li>
 * </ul>
 * </li>
 * <li>
 * 默认的insert与update语句,支持的注解：{@linkplain Nece},{@linkplain Unece},{@linkplain DBField}
 * <ul>
 * <li>
 * $[insert-part:【except={}】]:"(`column`,...) VALUES(#{column},...)"
 * </li>
 * <li>
 * $[update-part:【except={}】]:"`column`=#{column},..."
 * </li>
 * <li>
 * except:可选项、用于排除的数据库字段名(例如有的表妹remark字段)、多个用逗号隔开
 * </li>
 * </ul>
 * </li>
 * </ol>
 * <p>注解在Dao上</p>
 * Created by chenyg on 2018-03-06.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface MyBatisParams
{
    /**
     * 每一个为json格式
     *
     * @return
     */
    String[] value();
}
