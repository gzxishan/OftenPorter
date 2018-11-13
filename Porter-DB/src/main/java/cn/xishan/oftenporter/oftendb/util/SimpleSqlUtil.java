package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.db.CUnit;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.Operator;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.sql.SqlCondition;
import cn.xishan.oftenporter.oftendb.db.sql.SqlUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>说明：</p>
 * <ol>
 * <li>
 * 参数query支持的：<br>
 * <ul>
 * <li>name或$eq:name：等于。</li>
 * <li>$null:name：值为null</li>
 * <li>$notnull:name：值不为null</li>
 * <li>$emptystr:name：为空字符串</li>
 * <li>$gt:name(大于),$gte:name(大于等于),$lt:name(小于),$lte:name(小于等于),$ne:name(不等于)</li>
 * <li>$substr:name,$notsubstr:name：匹配包含或不包含某字符串</li>
 * <li>$startsWith:name,$notstartsWith:name：匹配以或不以某字符串开头</li>
 * <li>$endsWith:name,$notendsWith:name：匹配以或不以某字符串结尾</li>
 * <li>$in:name,$nin:name,$iin:name,$inin:name：值为数组或list，匹配在或不在指定列表中，
 * 注意当提供的数组或list为空时:'$in:name'变成'$false','$nin:name':'$true','$iin:'与'$inin:'会忽略。</li>
 * <li>$ignull:+其他，当为值空时忽略条件</li>
 * <li>$or[：或开始,</li>
 * <li>$or]：或结束,</li>
 * <li>$not[：非开始,</li>
 * <li>$not]：非结束,</li>
 * <li>$and[：与开始,</li>
 * <li>$and]：与结束</li>
 * <li>
 * $true与$false
 * </li>
 * </ul>
 * </li>
 * <li>
 * query中的特殊参数:$开头的其他参数,将会被忽略,最终在mybatis里通过query.$xxx获取
 * </li>
 * <li>
 * 参数order支持的：
 * [name,int,name,int,...]：name为排序字段，如"num","u.num"；int为排序方式，1升序，-1降序，其他默认。
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2017/12/12.
 */
public class SimpleSqlUtil
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSqlUtil.class);

    public static class SQLPart
    {
        /**
         * 参数map
         */
        public Map<String, Object> query;
        /**
         * WHERE子句,但无WHERE关键字
         */
        public String nowhere;
        /**
         * 完整的WHERE子句
         */
        public String where;
        /**
         * ORDER BY子句,但无ORDER BY关键字与LIMIT部分
         */
        public String noorder;

        /**
         * 完整的ORDER BY子句
         */
        public String order;
    }

    private String columnCoverString = "`";

    private static final SimpleSqlUtil INSTANCE = new SimpleSqlUtil();

    public static SimpleSqlUtil getInstance()
    {
        return INSTANCE;
    }

    public SimpleSqlUtil()
    {
    }

    public String getColumnCoverString()
    {
        return columnCoverString;
    }

    /**
     * 设置字段被包裹的内容，默认为"`"。
     *
     * @param columnCoverString
     */
    public void setColumnCoverString(String columnCoverString)
    {
        this.columnCoverString = columnCoverString;
    }

    public SQLPart from(TableOption tableOption)
    {
        List order = null;
        if (tableOption.settings != null)
        {
            order = tableOption.settings.getJSONArray("order");
        }
        return from(tableOption.query, tableOption.queryArray, order);
    }

    /**
     * @param query
     * @param nameValues 见{@linkplain #toQueryArray(Object...)}
     * @param order
     * @return
     */
    public SQLPart fromNameValues(Map query, List order, Object... nameValues)
    {
        return from(query, toQueryArray(nameValues), order);
    }


    static final String[] NO_VALUE_TAGS_PREFIX = {
            "$null:", "$notnull:", "$emptystr:"
    };
    static final String[] NO_VALUE_TAGS_ALL = {
            "$or[", "$or]", "$not[", "$not]", "$and[", "$and]", "$true", "$false"
    };

    static
    {
        Arrays.sort(NO_VALUE_TAGS_ALL);
    }

    /**
     * <p>
     * nameValues基本格式为(name,[value],name,[value]...),当name为($null:name,$notnull:name,$emptystr:name,$or[,$or],$not[,
     * $not],$and[,$and])中的一个时，value可以为null或者省略。
     * </p>
     * <p>
     * 如：toQueryArray("$gt:age",22,"$or[","$mark","1","$or]");
     * </p>
     * <p>
     * 另见{@linkplain TableOption#queryArray}
     * </p>
     *
     * @param nameValues
     * @return
     */
    public JSONArray toQueryArray(Object... nameValues)
    {
        JSONArray queryArray = new JSONArray();
        for (int i = 0; i < nameValues.length; )
        {
            if (OftenTool.isEmpty(nameValues[i]) || !(nameValues[i] instanceof String))
            {
                throw new IllegalArgumentException("illegal:index=" + i + ",element=" + nameValues[i]);
            }
            String name = (String) nameValues[i];
            boolean noValue = false;
            if (Arrays.binarySearch(NO_VALUE_TAGS_ALL, name) >= 0)
            {
                noValue = true;
            } else
            {
                for (String tag : NO_VALUE_TAGS_PREFIX)
                {
                    if (name.startsWith(tag))
                    {
                        noValue = true;
                        break;
                    }
                }
            }
            Object value = null;
            if (noValue)
            {
                if (i + 1 < nameValues.length && OftenTool.isEmpty(nameValues[i + 1]))
                {
                    i += 2;
                } else
                {
                    i++;
                }
            } else
            {
                if (i + 1 >= nameValues.length)
                {
                    throw new IllegalArgumentException("expected element at index:" + (i + 1));
                }
                value = nameValues[i + 1];
                i += 2;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", name);
            jsonObject.put("value", value);
            queryArray.add(jsonObject);
        }
        return queryArray;
    }

    /**
     * @param query
     * @param queryArray 见{@linkplain TableOption#queryArray}
     * @param order
     * @return
     */
    public SQLPart from(Map query, JSONArray queryArray, List order)
    {
        if (query == null)
        {
            query = new HashMap();
        } else
        {
            query = new HashMap(query);
        }
        String noWhere = toNoWhereStr(queryArray, query);
        String noOrderStr = toNoOrderStr(order);
        String orderStr = "";
        if (!noOrderStr.equals(""))
        {
            orderStr = "ORDER BY " + noOrderStr;
        }

        String whereStr = noWhere.equals("") ? "" : "WHERE " + noWhere;
        SQLPart sqlPart = new SQLPart();
        sqlPart.query = query;
        sqlPart.where = whereStr;
        sqlPart.nowhere = noWhere;
        sqlPart.order = orderStr;
        sqlPart.noorder = noOrderStr;
        // sqlPart.limit = noOrderStr[1];
        return sqlPart;
    }

    public String toOrderStr(List order)
    {
        String noOrderStr = toNoOrderStr(order);
        return OftenTool.isEmpty(noOrderStr) ? noOrderStr : "ORDER BY " + noOrderStr;
    }

    /**
     * 返回值"排序字段",不含ORDER BY
     *
     * @param order
     * @return
     */
    public String toNoOrderStr(List order)
    {
        String orderStr = "";
        if (order != null && order.size() > 0)
        {
            QuerySettings querySettings = new QuerySettings();
            for (int i = 0; i < order.size(); i += 2)
            {
                String field = (String) order.get(i);
                Integer n = (Integer) order.get(i + 1);
                if (n != null)
                {
                    querySettings.appendOrder(field, n);
                }
            }
            orderStr = SqlUtil.toOrder(querySettings, columnCoverString, false);
            String orderBy = "ORDER BY";
            int index = orderStr.indexOf(orderBy);
            orderStr = orderStr.substring(index + orderBy.length()).trim();
        }
        return orderStr;
    }

    private static final Pattern TYPE_PATTERN = Pattern.compile("^(\\$[^:]+:)");


    private static JSONArray combine(Map<String, Object> query, JSONArray queryArray)
    {
        if (queryArray == null)
        {
            queryArray = new JSONArray();
        } else
        {
            queryArray = new JSONArray(queryArray);
        }
        if (query != null)
        {
            for (Map.Entry entry : query.entrySet())
            {
                JSONObject json = new JSONObject(2);
                json.put("key", entry.getKey());
                json.put("value", entry.getValue());
                queryArray.add(json);
            }
        }
        return queryArray;
    }

    /**
     * 返回值不含WHERE关键字
     *
     * @param queryArray 见{@linkplain TableOption#queryArray}
     * @return
     */
    public String toNoWhereStr(JSONArray queryArray, Map<String, Object> forQuery)
    {
        if (forQuery == null)
        {
            throw new NullPointerException("forQuery param is null!");
        }
        queryArray = combine(forQuery, queryArray);
        if (queryArray.size() == 0)
        {
            return "";
        }
        SqlCondition rootCondition = new SqlCondition(columnCoverString);

        SqlCondition current = rootCondition;
        Stack<SqlCondition> conditionStack = new Stack<>();
        conditionStack.push(current);

        List<String> namesList = new ArrayList<>();
        String[] names = null;

        for (int k = 0; k < queryArray.size(); k++)
        {
            JSONObject json = queryArray.getJSONObject(k);
            Object value = json.get("value");
            String name = json.getString("key");

            if (name.equals("$and["))
            {
                SqlCondition and = new SqlCondition(columnCoverString);
                current.append(Condition.AND, and);
                current = and;
                conditionStack.push(current);
                continue;
            } else if (name.equals("$or["))
            {
                SqlCondition or = new SqlCondition(columnCoverString);
                current.append(Condition.OR, or);
                current = or;
                conditionStack.push(current);
                continue;
            } else if (name.equals("$not["))
            {
                SqlCondition not = new SqlCondition(columnCoverString);
                current.append(Condition.NOT, not);
                current = not;
                conditionStack.push(current);
                continue;
            } else if (OftenStrUtil.indexOf(name, "$and]", "$or]", "$not]") >= 0)
            {
                conditionStack.pop();
                current = conditionStack.peek();
                continue;
            } else if (name.startsWith("$ignull:"))
            {
                if (OftenTool.isEmpty(value))
                {
                    //忽略空值
                    continue;
                }
                name = name.substring(8);
            }

            boolean willAddName = true;
            Operator operator = Condition.EQ;
            int index = 0;
            if (OftenTool.isEmpty(value))
            {
                String oldName = name;
                if (name.startsWith("$ne:"))
                {
                    name = "$notnull:" + name.substring(4);
                    LOGGER.warn("change sql op:{} -> {}", oldName, name);
                } else if (name.startsWith("$eq:"))
                {
                    name = "$null:" + name.substring(4);
                    LOGGER.warn("change sql op:{} -> {}", oldName, name);
                }
            }

            Matcher matcher = TYPE_PATTERN.matcher(name);
            boolean matched = matcher.find();
            if (!matched && OftenTool.isEmpty(value))
            {
                operator = SqlCondition.IS_NULL;
                willAddName = false;
            } else if (matched)
            {
                String type = matcher.group(1);
                switch (type)
                {
                    case "$eq:":
                        operator = Condition.EQ;
                        break;
                    case "$gte:":
                        operator = Condition.GTE;
                        break;
                    case "$gt:":
                        operator = Condition.GT;
                        break;
                    case "$lte:":
                        operator = Condition.LTE;
                        break;
                    case "$lt:":
                        operator = Condition.LT;
                        break;
                    case "$ne:":
                        operator = Condition.NE;
                        break;
                    case "$substr:":
                        operator = Condition.SUBSTR;
                        break;
                    case "$notsubstr:":
                        operator = Condition.NOTSUBSTR;
                        break;
                    case "$startsWith:":
                        operator = Condition.STARTSWITH;
                        break;
                    case "$notstartsWith:":
                        operator = Condition.NOTSTARTSWITH;
                        break;
                    case "$endsWith:":
                        operator = Condition.ENDSSWITH;
                        break;
                    case "$notendsWith:":
                        operator = Condition.NOTENDSSWITH;
                        break;
                    case "$null:":
                        operator = SqlCondition.IS_NULL;
                        willAddName = false;
                        break;
                    case "$notnull:":
                        operator = SqlCondition.IS_NOT_NULL;
                        willAddName = false;
                        break;
                    case "$emptystr:":
                        operator = Condition.EQ;
                        value = "";
                        break;
                    case "$false":
                        operator = SqlCondition.FALSE;
                        willAddName = false;
                        break;
                    case "$true":
                        operator = SqlCondition.TRUE;
                        willAddName = false;
                        break;
                    case "$in:":
                    case "$nin:":
                    case "$iin:":
                    case "$inin:":
                    {
                        Object[] array = null;
                        if (value != null)
                        {
                            if (value instanceof List)
                            {
                                array = ((List) value).toArray(new Object[0]);
                            } else if (value instanceof Object[])
                            {
                                array = (Object[]) value;
                            } else
                            {
                                array = new Object[]{value};
                            }
                        }
                        if (array == null || array.length == 0)
                        {
                            if (type.equals("$iin:") || type.equals("$inin:"))
                            {
                                LOGGER.warn("ignore in or nin when empty:{}{}", type, name);
                                continue;//忽略
                            }
                            if (type.equals("$in:"))
                            {
                                operator = SqlCondition.FALSE;
                            } else
                            {
                                operator = SqlCondition.TRUE;
                            }
                            LOGGER.warn("change sql op:{}{} -> {}", type, name, operator == SqlCondition.TRUE);
                            willAddName = false;
                        } else
                        {
                            if (type.equals("$in:") || type.equals("$iin:"))
                            {
                                operator = Condition.IN;
                            } else
                            {
                                operator = Condition.NIN;
                            }
                            name = name.substring(type.length());
                            type = "";
                            names = new String[array.length];
                            for (int i = 0; i < names.length; i++)
                            {
                                names[i] = name + "[" + i + "]_" + k;
                            }

                        }

                    }
                    break;
                    default:
                }
                index = type.length();
            } else if (name.startsWith("$"))
            {
                forQuery.put(name, value);
                continue;
            }

            name = name.substring(index);
            String queryName = name + "_" + k;
            if (willAddName)
            {
                if (names == null)
                {
                    names = new String[]{queryName};
                }
                for (String nameStr : names)
                {
                    namesList.add(nameStr.replace('$', '_').replace(':', '_'));
                }
            }

            names = null;
            current.append(operator, new CUnit(name, value));
            forQuery.put(queryName, value);
        }
        //内部会对name进行检测
        Object[] objs = (Object[]) rootCondition.toFinalObject();
        String sql = (String) objs[0];
        Object[] args = (Object[]) objs[1];
        String where;
        if (OftenTool.notNullAndEmpty(sql) && namesList.size() > 0)
        {
            StringBuilder stringBuilder = new StringBuilder();

            int index2 = 0;
            for (int i = 0, index1 = 0; i < namesList.size(); i++)
            {
                String name = namesList.get(i);
                name = name.replace(".", "_xs_").replace("[", "$_").replace("]", "_$");
                forQuery.put(name, args[i]);
                index2 = sql.indexOf('?', index1);
                stringBuilder.append(sql, index1, index2);
                stringBuilder.append("#{").append("query.").append(name).append("}");
                index1 = index2 + 1;
            }
            stringBuilder.append(sql.substring(index2 + 1));
            where = stringBuilder.toString();
        } else
        {
            where = sql == null ? "" : sql;
        }
        return where;
    }
}
