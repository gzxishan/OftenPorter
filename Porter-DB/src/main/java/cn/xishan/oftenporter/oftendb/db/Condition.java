package cn.xishan.oftenporter.oftendb.db;

import java.util.ArrayList;
import java.util.List;

public abstract class Condition implements ToFinal
{

    /**
     * 大于
     */
    public static final Operator GT = new MyOperator("GT");

    /**
     * 大于等于
     */
    public static final Operator GTE = new MyOperator("GTE");

    /**
     * 小于
     */
    public static final Operator LT = new MyOperator("LT");

    /**
     * 小于等于
     */
    public static final Operator LTE = new MyOperator("LTE");

    /**
     * 不等于
     */
    public static final Operator NE = new MyOperator("NE");

    /**
     * 等于
     */
    public static final Operator EQ = new MyOperator("EQ");

    /**
     * in:值为Object[],List或Object
     */
    public static final Operator IN = new MyOperator("IN");

    /**
     * not in:值为Object[],List或Object
     */
    public static final Operator NIN = new MyOperator("NIN");

    /**
     * 子串查询
     */
    public static final Operator SUBSTR = new MyOperator("SUBSTR");

    /**
     * 以内容开头
     */
    public static final Operator STARTSWITH = new MyOperator("STARTSWITH");

    /**
     * 以内容结尾
     */
    public static final Operator ENDSSWITH = new MyOperator("ENDSSWITH");

    /**
     * 或者,对应于{@linkplain #put(Operator, Condition)},使得put变成逻辑或连接。
     */
    public static final Operator OR = new MyOperator("OR");

    /**
     * 非,对应于{@linkplain #put(Operator, Condition)},使得条件结果整体取反。
     */
    public static final Operator NOT = new MyOperator("NOT");

    public static class MyOperator extends Operator
    {
        private static int count;
        private int id;
        private String name;

        public MyOperator(String name)
        {
            id = count++;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            return id;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj != null && (obj instanceof MyOperator))
            {
                MyOperator myOperator = (MyOperator) obj;
                return myOperator.id == id;
            }
            return false;
        }
    }

    /**
     * 从字符串中转换:大于、大于等于、小于、小于等于、等于和不等于
     *
     * @param op
     * @return
     */
    public static Operator fromStr(String op)
    {
        op = op.toLowerCase().replaceAll(" ", "");
        Operator operator = null;
        if (op.equals("substr"))
        {
            operator = Condition.SUBSTR;
        } else if (op.equals("in"))
        {
            operator = Condition.IN;
        } else if (op.equals(">"))
        {
            operator = Condition.GT;
        } else if (op.equals(">="))
        {
            operator = Condition.GTE;
        } else if (op.equals("<"))
        {
            operator = Condition.LT;
        } else if (op.equals("<="))
        {
            operator = Condition.LTE;
        } else if (op.equals("!="))
        {
            operator = Condition.NE;
        } else if (op.equals("="))
        {
            operator = Condition.EQ;
        }
        return operator;

    }

    /**
     * 转换为符合格式的条件判断语句,会忽略所有为null的字段. <br>
     *
     * @return
     */
    public abstract Object toFinalObject() throws ConditionException;

    /**
     * 处理字段(含有@Key注解的).
     *
     * @param c 用于Field查找的类
     */
    public abstract void dealNames(Class<?> c);

    public static class ConditionException extends RuntimeException
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public ConditionException()
        {
        }

        public ConditionException(String info)
        {
            super(info);
        }

        public ConditionException(Throwable throwable)
        {
            super(throwable);
        }

    }

    private List<Operator> operators = new ArrayList<Operator>();
    private List<Object> list = new ArrayList<Object>();

    public int size()
    {
        return operators.size();
    }

    public Operator getOperator(int index)
    {
        return operators.get(index);
    }

    public Object get(int index)
    {
        return list.get(index);
    }

    private void isNull(Object object)
    {
        if (object == null)
        {
            throw new NullPointerException();
        }
    }

    /**
     * 同{@linkplain #append(Operator, String, Object) append(Condition.EQ, String, Object)}
     *
     * @param cunitName
     * @param value
     * @return
     */
    public Condition append(String cunitName, Object value)
    {
        return append(EQ, cunitName, value);
    }

    public Condition append(Operator operator, String cunitName, Object cunitValue)
    {
        return put(operator, new CUnit(cunitName, cunitValue));
    }

    /**
     * 请使用{@linkplain #append(Operator, String, Object)}
     *
     * @param operator
     * @param cunitName
     * @param cunitValue
     * @return
     */
    @Deprecated
    public Condition put(Operator operator, String cunitName, Object cunitValue)
    {
        return put(operator, new CUnit(cunitName, cunitValue));
    }

    /**
     * 请使用{@linkplain #put(Operator, CUnit)}
     *
     * @param operator
     * @param cUnit
     * @return
     */
    @Deprecated
    public Condition put(Operator operator, CUnit cUnit)
    {
        return append(operator, cUnit);
    }

    /**
     * 默认为逻辑与。
     *
     * @param operator
     * @param cUnit
     * @return
     */
    public Condition append(Operator operator, CUnit cUnit)
    {
        isNull(operator);
        isNull(cUnit);
        if (operator == NOT || operator == OR)
        {
            throw new IllegalArgumentException("the operator should not be " + operator);
        }
        operators.add(operator);
        list.add(cUnit);
        return this;
    }

    public void clear()
    {
        operators.clear();
        list.clear();
    }


    /**
     * 请使用{@linkplain #append(Operator, Condition)}
     *
     * @param operator
     * @param condition
     * @return
     */
    @Deprecated
    public Condition put(Operator operator, Condition condition)
    {
        return append(operator, condition);
    }

    /**
     * @param operator  使用{@linkplain #OR}、{@linkplain #NOT}等.
     * @param condition
     */
    public Condition append(Operator operator, Condition condition)
    {
        isNull(operator);
        isNull(condition);
        if (operator != NOT && operator != OR)
        {
            throw new IllegalArgumentException("the operator should be " + operator);
        }
        operators.add(operator);
        list.add(condition);
        return this;
    }

}
