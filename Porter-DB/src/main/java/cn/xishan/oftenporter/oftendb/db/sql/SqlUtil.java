package cn.xishan.oftenporter.oftendb.db.sql;


import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.QuerySettings.Order;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil
{


    public static class WhereSQL
    {
        public String sql;
        public Object[] args;

        public WhereSQL()
        {
        }

        public WhereSQL(String sql, Object... args)
        {
            this.sql = sql;
            this.args = args;
        }
    }

//    /**
//     * 把字段field的值value转换为字符串,对String会进行sql防注入处理。
//     *
//     * @param typeName
//     * @param value
//     * @return
//     */
//    public static String checkStr(String typeName, Object value)
//    {
//        if (value == null)
//        {
//            return null;
//        }
//        String s;
//        String cName = typeName;
//
//        if (value instanceof String)
//        {
//            value = ((String) value).replaceAll(".*([';]+|(--)+).*", "_");
//        }
//
//        // /
//        if (cName.equals(String.class.getName()) || cName.equals("char")
//                || cName.equals(Character.class.getName())
//                || cName.equals(JSONObject.class.getName())
//                || cName.equals(JSONArray.class.getName()))
//        {
//            s = "'" + value + "'";
//        } else if (cName.equals("boolean") || cName.equals(Boolean.class.getName()))
//        {
//            s = ((Boolean) value ? 1 : 0) + "";
//        } else
//        {
//            s = value.toString();
//        }
//
//        return s;
//    }
//
//    /**
//     * 对String会进行sql防注入处理。
//     *
//     * @param value
//     * @return
//     */
//    public static String checkStr(Object value)
//    {
//        if (value == null)
//        {
//            return null;
//        }
//        String s;
//        String cName = value.getClass().getName();
//
//        if (value instanceof String)
//        {
//            value = ((String) value).replaceAll(".*([';]+|(--)+).*", "_");
//        }
//
//        // /
//        if (cName.equals(String.class.getName()) || cName.equals("char")
//                || cName.equals(Character.class.getName())
//                || cName.equals(JSONObject.class.getName())
//                || cName.equals(JSONArray.class.getName()))
//        {
//            s = "'" + value + "'";
//        } else if (cName.equals("boolean") || cName.equals(Boolean.class.getName()))
//        {
//            s = ((Boolean) value ? 1 : 0) + "";
//        } else
//        {
//            s = value.toString();
//        }
//
//        return s;
//    }

    /**
     * 转换成insert或replace的sql句，参数值用?表示
     *
     * @param isInsert
     * @param tableName
     * @param names
     * @param withSemicolon 是否以分号结尾
     * @return
     */
    public static String toInsertOrReplace(boolean isInsert, String tableName, String[] names, String coverString,
            boolean withSemicolon)
    {

        StringBuilder nameBuilder = new StringBuilder(), valueBuilder = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            nameBuilder.append(coverString).append(names[i]).append(coverString).append(",");
            valueBuilder.append("?,");
        }

        if (nameBuilder.length() > 0)
        {
            BaseEasier.removeEndChar(nameBuilder, ',');
            BaseEasier.removeEndChar(valueBuilder, ',');
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(isInsert ? "INSERT" : "REPLACE").append(" INTO ").append(coverString).append(tableName)
                .append(coverString);

        stringBuilder.append(" (").append(nameBuilder).append(") VALUES(").append(valueBuilder).append(")");
        if (withSemicolon)
        {
            stringBuilder.append(';');
        }

        return stringBuilder.toString();

    }

    /**
     * set的sql语句，参数值用?表示
     *
     * @param tableName
     * @param names
     * @param basicCondition
     * @return
     */
    public static WhereSQL toSetValues(String tableName, String[] names, Condition basicCondition, String coverString,
            boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder setValues = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            setValues.append(coverString).append(name).append(coverString).append("=").append("?,");
        }

        if (setValues.length() > 0)
        {
            BaseEasier.removeEndChar(setValues, ',');
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(coverString).append(tableName).append(coverString).append(" SET ")
                .append(setValues);
        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = OftenTool.EMPTY_OBJECT_ARRAY;
        }
        if (withSemicolon)
            stringBuilder.append(";");
        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    public static WhereSQL toUpdate(String tableName, Condition condition, String setName, String coverString,
            boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE  ").append(coverString).append(tableName).append(coverString).append(" SET ")
                .append(coverString).append(setName).append(coverString).append("=?");

        if (condition != null)
        {
            Object[] result = (Object[]) condition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = OftenTool.EMPTY_OBJECT_ARRAY;
        }

        if (withSemicolon)
        {
            stringBuilder.append(";");
        }

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    public static WhereSQL toDelete(String tableName, Condition condition, String coverString, boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM  ").append(coverString).append(tableName).append(coverString);

        if (condition != null)
        {
            Object[] result = (Object[]) condition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = OftenTool.EMPTY_OBJECT_ARRAY;
        }

        if (withSemicolon)
        {
            stringBuilder.append(";");
        }

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }


    /**
     * 转换成sql语句，参数用？代替
     */
    public static WhereSQL toCountSelect(WhereSQL whereSQL, String columnName,
            boolean withSemicolon)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT count(1) ").append(columnName).append(" FROM (").append(whereSQL.sql).append(") temp");
        if (withSemicolon)
        {
            builder.append(";");
        }
        WhereSQL where = new WhereSQL(builder.toString(), whereSQL.args);
        return where;
    }

    /**
     * 转换成sql语句，参数用？代替
     */
    public static WhereSQL toCountSelect(String tableName, String columnName, Condition basicCondition,
            String coverString,
            boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(1) ").append(columnName);
        stringBuilder.append(" FROM ").append(coverString).append(tableName).append(coverString);

        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();
            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = OftenTool.EMPTY_OBJECT_ARRAY;
        }

        if (withSemicolon)
        {
            stringBuilder.append(";");
        }

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    /**
     * 转换成sql语句，参数用？代替
     */
    public static WhereSQL toSelect(String tableName, Condition basicCondition, QuerySettings querySettings,
            String coverString,
            boolean withSemicolon,
            String... keys)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        if (keys == null || keys.length == 0)
        {
            stringBuilder.append("*");
        } else
        {
            for (String key : keys)
            {
                stringBuilder.append(coverString).append(key).append(coverString).append(",");
            }
        }

        if (stringBuilder.charAt(stringBuilder.length() - 1) == ',')
        {
            BaseEasier.removeEndChar(stringBuilder, ',');
        }
        stringBuilder.append(" FROM ").append(coverString).append(tableName).append(coverString);


        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = OftenTool.EMPTY_OBJECT_ARRAY;
        }

        String order = toOrder(querySettings, coverString, withSemicolon);

        stringBuilder.append(order);

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }


    /**
     * {@linkplain Order#name}只允许【字母、数字、下划线、点号、中文】
     *
     * @param settings
     * @param coverString
     * @return
     */
    public static Object toFinalObject(QuerySettings settings, String coverString)
    {
        if (settings == null || settings.getOrders().size() == 0)
        {
            return null;
        }
        List<Order> orders = settings.getOrders();

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < orders.size(); i++)
        {
            Order order = orders.get(i);

            //只允许字母、数字、下划线、点号、中文
            String name = order.name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_.]", "");

            SqlCondition.appendName(coverString, name, sbuilder);
            if (order.n == 1)
            {
                sbuilder.append(" ASC");
            } else
            {
                sbuilder.append(" DESC");
            }
            sbuilder.append(",");
        }
        if (sbuilder.length() > 0)
        {
            BaseEasier.removeEndChar(sbuilder, ',');
        }
        return sbuilder;
    }

    /**
     * {@linkplain Order#name}只允许【字母、数字、下划线、点号、中文】
     *
     * @param querySettings
     * @param coverString 包裹变量的字符，如【`】
     * @param withSemicolon
     * @return
     */
    public static String toOrder(QuerySettings querySettings, String coverString, boolean withSemicolon)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (querySettings != null)
        {
            Object object = toFinalObject(querySettings, coverString);
            if (object != null)
            {
                stringBuilder.append(" ORDER BY ").append(object);
            }
            if (querySettings.getLimit() != null)
            {
                int offset = querySettings.getSkip() == null ? 0
                        : querySettings.getSkip();
                int count = querySettings.getLimit();
                stringBuilder.append(" LIMIT ").append(offset).append(",").append(count);
            }
        }

        if (withSemicolon)
        {
            stringBuilder.append(";");
        }
        return stringBuilder.toString();
    }

    /**
     * 转义相对于like的特殊字符。
     *
     * @param content
     * @return
     */
    public static String filterLike(String content)
    {
        content = content.replaceAll("\\[", "[[]");
        content = content.replaceAll("%", "[%]").replaceAll("\\^", "[^]").replaceAll("_", "[_]");
        return content;
    }


    public static class CreateTable
    {
        private String tableName;
        private String createTableSql;

        public CreateTable(String tableName, String createTableSql)
        {
            this.tableName = tableName;
            this.createTableSql = createTableSql;
        }

        public String getTableName()
        {
            return tableName;
        }

        public void setTableName(String tableName)
        {
            this.tableName = tableName;
        }

        public String getCreateTableSql()
        {
            return createTableSql;
        }

        public void setCreateTableSql(String createTableSql)
        {
            this.createTableSql = createTableSql;
        }

        @Override
        public String toString()
        {
            return createTableSql;
        }
    }

    /**
     * @param tableNamePattern
     * @param host             包括端口,如localhost:3306
     * @param dbname
     * @param mysqlUser
     * @param mysqlPassword
     * @return
     */
    public static List<CreateTable> exportCreateTable(String tableNamePattern, String host, String dbname,
            String mysqlUser, String mysqlPassword, String coverString)
    {
        try
        {
            return exportCreateTable(tableNamePattern,
                    "jdbc:sql://" + host + "/" + dbname + "?user=" + URLEncoder
                            .encode(mysqlUser, "utf-8") + "&password=" + URLEncoder.encode(mysqlPassword, "utf-8"),
                    "com.sql.jdbc.Driver", coverString);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param tableNamePattern 为null表示导出所有的。
     * @param connectionUrl
     * @param driverClass
     * @return
     */
    public static List<CreateTable> exportCreateTable(String tableNamePattern, String connectionUrl, String driverClass,
            String coverString)
    {
        Connection conn = null;
        try
        {
            Class.forName(driverClass);
            conn = DriverManager.getConnection(connectionUrl);

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData
                    .getTables(null, "%", tableNamePattern == null ? "%" : tableNamePattern, new String[]{"TABLE"});

            List<CreateTable> list = new ArrayList<>();

            if (tables.next())
            {
                PreparedStatement ps0 = conn.prepareStatement("set sql_quote_show_create=1;");
                ps0.execute();
                ps0.close();
                do
                {
                    String tableName = tables.getString("TABLE_NAME");
                    PreparedStatement ps = conn
                            .prepareStatement("SHOW CREATE TABLE " + coverString + tableName + coverString);
                    ResultSet resultSet = ps.executeQuery();
                    resultSet.next();
                    String createTable = resultSet.getString("Create Table");
                    list.add(new CreateTable(tableName, createTable));
                    resultSet.close();
                    ps.close();
                } while (tables.next());


            }
            tables.close();
            return list;
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            OftenTool.close(conn);
        }


    }


}
