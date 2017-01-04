package cn.xishan.oftenporter.oftendb.db.mongodb;


import cn.xishan.oftenporter.oftendb.db.Operator;
import cn.xishan.oftenporter.oftendb.db.Unit;

public class MOperator
{

    static class _Operator extends Operator
    {
        private String value;
        private boolean isForUnit;

        public _Operator(String value, boolean isForUnit)
        {
            this.value = value;
            this.isForUnit = isForUnit;
        }

        public boolean isForUnit()
        {
            return isForUnit;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }


    /**
     * @param value
     * @param forUnit 是否是用于{@linkplain Unit}
     * @return
     */
    public static Operator newOperator(String value, boolean forUnit)
    {
        return new _Operator(value, forUnit);
    }
}
