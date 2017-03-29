package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.CUnit;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.Operator;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.ArrayList;
import java.util.List;

public class SqlCondition extends Condition
{

    private boolean isAnd = true;
    private Class<?> dealNamesClass;

    /**
     * <pre>
     * 通配符 说明
     * _    与任意单字符匹配
     * %    与包含一个或多个字符的字符串匹配
     * [ ]  与特定范围（例如，[a-f]）或特定集（例如，[abcdef]）中的任意单字符匹配。
     * [^]  与特定范围（例如，[^a-f]）或特定集（例如，[^abcdef]）之外的任意单字符匹配。
     * </pre>
     */
    public static final Operator LIKE = new MyOperator("LIKE");

    public static final Operator IS_NULL = new MyOperator("is NULL");

    public static final Operator IS_NOT_NULL = new MyOperator("is not NULL");


    /**
     * 返回为一个Object[]{whereSql,args}
     *
     * @return
     * @throws ConditionException
     */
    @Override
    public Object toFinalObject() throws ConditionException
    {
        return _toFinalObject();
    }

    /**
     * 会进行sql注入处理.
     */
    private Object[] _toFinalObject() throws ConditionException
    {
        StringBuilder stringBuilder = new StringBuilder();
        List<Object> args = new ArrayList<>(size());
        for (int i = 0; i < size(); i++)
        {
            Operator operator = getOperator(i);
            if (operator == NOT || operator == OR)
            {
                deal2(operator, get(i), stringBuilder, args);
            } else
            {
                dealNormal(operator, get(i), stringBuilder, args);
            }
        }

        if (stringBuilder.length() == 0)
        {
            stringBuilder.append(" TRUE ");
        }

        StringBuilder result = new StringBuilder();
        List<Object> list = new ArrayList<>(args.size());

        int index1 = 0, index2 = 0;
        while (true)
        {
            index1 = stringBuilder.indexOf("{", index2);
            if (index1 == -1)
            {
                result.append(stringBuilder.subSequence(index2, stringBuilder.length()));
                break;
            } else
            {
                result.append(stringBuilder.subSequence(index2, index1));
            }

            index2 = stringBuilder.indexOf("}", index1 + 1);
            result.append('?');
            list.add(args.get(Integer.parseInt(stringBuilder.substring(index1 + 1, index2))));
            if (++index2 >= stringBuilder.length())
            {
                break;
            }
        }
        return new Object[]{result.toString(), list.toArray(new Object[0])};
    }


    @Override
    public String toString()
    {
        Object obj = toFinalObject();

        return obj != null ? obj.toString() : super.toString();
    }

    private void deal2(Operator operator, Object object, StringBuilder stringBuilder, List<Object> args)
    {
        if (!(object instanceof SqlCondition))
        {
            throw new ConditionException("value should be type of " + getClass()
                    + ".Current type is "
                    + object.getClass());
        }
        SqlCondition condition = (SqlCondition) object;

        link(stringBuilder);//and或or连接

        if (operator == NOT)
        {
            stringBuilder.append(" NOT ");

        } else if (operator == OR)
        {
            condition.isAnd = false;
        } else
        {
            throw new ConditionException("the operator should be " + NOT
                    + " or "
                    + OR
                    + " for value of " + getClass().getSimpleName() + " type");
        }
        Object[] sqlArgs = (Object[]) condition.toFinalObject();
        Object[] _args = (Object[]) sqlArgs[1];
        String sql = (String) sqlArgs[0];
        stringBuilder.append("(");
        for (int i = 0, k = 0; i < sql.length(); i++)
        {
            char c = sql.charAt(i);
            if (c == '?')
            {
                stringBuilder.append("{").append(args.size()).append("}");
                args.add(_args[k++]);
            } else
            {
                stringBuilder.append(c);
            }
        }
        stringBuilder.append(")");
    }

    /**
     * and 或 or 连接。
     *
     * @param stringBuilder
     */
    private void link(StringBuilder stringBuilder)
    {
        if (stringBuilder.length() > 0)
        {
            stringBuilder.append(isAnd ? " AND " : " OR ");
        }
    }

    /**
     * @param CUnit
     * @Key注解的处理
     */
    private void dealNames(CUnit CUnit)
    {
        if (dealNamesClass == null)
        {
            return;
        }
        if (!CUnit.isParam1Value())
        {
            CUnit.setParam1(BaseEasier.dealWith_Key(dealNamesClass, CUnit.getParam1()));
        }
        if (!CUnit.isParam2Value())
        {
            CUnit.setParam2(BaseEasier.dealWith_Key(dealNamesClass, (String) CUnit.getParam2()));
        }
    }

    /**
     * 从字符串转换出Operator
     *
     * @param op
     * @return
     */
    public static Operator fromStr(String op)
    {
        op = op.toLowerCase();
        Operator operator = Condition.fromStr(op);
        if (operator != null)
        {
            return operator;
        }
        if (op.equals("like"))
        {
            operator = LIKE;
        } else if (op.equals("is null"))
        {
            operator = IS_NULL;
        } else if (op.equals("is not null"))
        {
            operator = IS_NOT_NULL;
        } else
        {
            throw new ConditionException("unknown operator " + op);
        }
        return operator;
    }

    private void checkName(String name)
    {
        if (WPTool.isEmpty(name) || name.indexOf('{') >= 0 || name.indexOf('}') >= 0)
        {
            throw new RuntimeException("illegal name of '" + name + "'");
        }
    }

    private void dealNormal(Operator operator, Object object, StringBuilder stringBuilder, List<Object> args)
    {
        if (!(object instanceof CUnit))
        {
            throw new ConditionException(operator + "-CUnit is accept!");
        }
        CUnit cUnit = (CUnit) object;

        dealNames(cUnit);// @Key注解的处理

        link(stringBuilder);//and或or
        if (cUnit.isParam1Value())
        {
            stringBuilder.append("{").append(args.size()).append("}");
            args.add(cUnit.getParam1());
        } else
        {
            checkName(cUnit.getParam1());
            stringBuilder.append("`").append(cUnit.getParam1()).append("`");
        }
        stringBuilder.append(" ");

        if (operator == IS_NOT_NULL)
        {
            stringBuilder.append("is not NULL ");
            return;
        } else if (operator == IS_NULL//添加旁边的或者使得当查询条件的value为null时，使用is NULL
                || (cUnit.getParam2() == null && operator == EQ && !cUnit.isParam1Value() && cUnit.isParam2Value()))
        {
            operator = IS_NULL;
            stringBuilder.append("is NULL ");
            return;
        }

        if (operator == IN || operator == NIN)
        {
            StringBuilder sBuilder = new StringBuilder("(");
            if (cUnit.getParam2() instanceof Object[] || cUnit.getParam2() instanceof List)
            {
                Object[] objects;
                if (cUnit.getParam2() instanceof List)
                {
                    List list = (List) cUnit.getParam2();
                    objects = list.toArray(new Object[0]);
                } else
                {
                    objects = (Object[]) cUnit.getParam2();
                }
                if (objects != null)
                {
                    for (int i = 0; i < objects.length - 1; i++)
                    {
                        sBuilder.append("{").append(args.size()).append("},");
                        args.add(objects[i]);
                    }
                    if (objects.length > 0)
                    {
                        sBuilder.append("{").append(args.size()).append("}");
                        args.add(objects[objects.length - 1]);
                    }
                }
            } else
            {
                sBuilder.append("{").append(args.size()).append("}");
                args.add(cUnit.getParam2());
            }

            sBuilder.append(") ");
            stringBuilder.append(operator == IN ? "in " : "not in ").append(sBuilder);
            return;
        }

        if (operator == GT)
        {
            stringBuilder.append(">");
        } else if (operator == GTE)
        {
            stringBuilder.append(">=");
        } else if (operator == LT)
        {
            stringBuilder.append("<");
        } else if (operator == LTE)
        {
            stringBuilder.append("<=");
        } else if (operator == NE)
        {
            stringBuilder.append("!=");
        } else if (operator == EQ)
        {
            stringBuilder.append("=");
        } else if (operator == LIKE)
        {
            stringBuilder.append("LIKE");
        } else if (operator == SUBSTR)
        {
            stringBuilder.append("LIKE ").append("{").append(args.size()).append("}");
            args.add("%" + SqlUtil.fileterLike(cUnit.getParam2() + "") + "%");
            return;
        } else if (operator == STARTSWITH)
        {
            stringBuilder.append("LIKE ").append("{").append(args.size()).append("}");
            args.add(SqlUtil.fileterLike(cUnit.getParam2() + "") + "%");
            return;
        } else if (operator == ENDSSWITH)
        {
            stringBuilder.append("LIKE ").append("{").append(args.size()).append("}");
            args.add("%" + SqlUtil.fileterLike(cUnit.getParam2() + ""));
            return;
        } else
        {
            throw new ConditionException("unknown operator " + operator);
        }
        stringBuilder.append(" ");
        if (cUnit.isParam2Value())
        {
            stringBuilder.append("{").append(args.size()).append("}");
            args.add(cUnit.getParam2());
        } else
        {
            checkName((String) cUnit.getParam2());
            stringBuilder.append("`").append(cUnit.getParam2()).append("`");
        }
    }

    @Override
    public void dealNames(Class<?> c)
    {
        this.dealNamesClass = c;
    }
}
