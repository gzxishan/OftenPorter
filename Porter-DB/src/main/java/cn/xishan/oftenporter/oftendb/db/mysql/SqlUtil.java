package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;

public class SqlUtil
{

    private static final Object[] EMPTY_OBJS = new Object[0];

    public static class WhereSQL
    {
        public String sql;
        public Object[] args;
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
    public static String toInsertOrReplace(boolean isInsert, String tableName, String[] names, boolean withSemicolon)
    {

        StringBuilder nameBuilder = new StringBuilder(), valueBuilder = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            nameBuilder.append("`").append(names[i]).append("`,");
            valueBuilder.append("?,");
        }

        if (nameBuilder.length() > 0)
        {
            BaseEasier.removeEndChar(nameBuilder, ',');
            BaseEasier.removeEndChar(valueBuilder, ',');
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(isInsert ? "INSERT" : "REPLACE").append(" INTO `").append(tableName).append('`');

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
    public static WhereSQL toSetValues(String tableName, String[] names, Condition basicCondition,
            boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder setValues = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            setValues.append('`').append(name).append("`=").append("?,");
        }

        if (setValues.length() > 0)
        {
            BaseEasier.removeEndChar(setValues, ',');
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE `").append(tableName).append("` SET ").append(setValues);
        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = EMPTY_OBJS;
        }
        if (withSemicolon)
            stringBuilder.append(";");
        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    public static WhereSQL toUpdate(String tableName, Condition condition, String setName, boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE  `").append(tableName).append("` SET `").append(setName).append("`=?");

        if (condition != null)
        {
            Object[] result = (Object[]) condition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = EMPTY_OBJS;
        }

        if (withSemicolon)
        {
            stringBuilder.append(";");
        }

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    public static WhereSQL toDelete(String tableName, Condition condition, boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM  `").append(tableName).append("`");

        if (condition != null)
        {
            Object[] result = (Object[]) condition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = EMPTY_OBJS;
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
    public static WhereSQL toCountSelect(String tableName, String columnName, Condition basicCondition,
            boolean withSemicolon)
    {
        WhereSQL whereSQL = new WhereSQL();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(*) ").append(columnName);
        stringBuilder.append(" FROM `").append(tableName).append('`');

        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();
            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = EMPTY_OBJS;
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
                stringBuilder.append('`').append(key).append("`,");
            }
        }

        if (stringBuilder.charAt(stringBuilder.length() - 1) == ',')
        {
            BaseEasier.removeEndChar(stringBuilder, ',');
        }
        stringBuilder.append(" FROM `").append(tableName).append('`');


        if (basicCondition != null)
        {
            Object[] result = (Object[]) basicCondition.toFinalObject();

            stringBuilder.append(" WHERE ").append(result[0]);
            whereSQL.args = (Object[]) result[1];
        } else
        {
            whereSQL.args = EMPTY_OBJS;
        }

        if (querySettings != null)
        {
            Object object = querySettings.toFinalObject();
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

        whereSQL.sql = stringBuilder.toString();
        return whereSQL;
    }

    public static String fileterLike(String content)
    {
        content = content.replaceAll("\\[", "[[]");
        content = content.replaceAll("%", "[%]").replaceAll("\\^", "[^]").replaceAll("_", "[_]");
        return content;
    }

}
