package cn.xishan.oftenporter.oftendb.db.mongodb;


import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.Operator;
import cn.xishan.oftenporter.oftendb.db.Unit;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoCondition extends Condition
{

    private Class<?> dealNamesClass;

    @Override
    public String toString()
    {
        Object obj = toFinalObject();
        return obj != null ? obj.toString() : super.toString();
    }

    /**
     * 会进行sql注入处理.
     */
    @Override
    public Object toFinalObject() throws ConditionException
    {

        DBObject baseObject = new BasicDBObject();

        for (int i = 0; i < size(); i++)
        {
            Operator operator = getOperator(i);
            put(operator, get(i), baseObject);
        }

        return baseObject;
    }

    /**
     * 用于$or
     *
     * @return
     */
    private BasicDBList toDBList()
    {
        BasicDBList dbList = new BasicDBList();
        for (int i = 0; i < size(); i++)
        {
            DBObject dbObject = new BasicDBObject();
            Operator operator = getOperator(i);
            put(operator, get(i), dbObject);
            dbList.add(dbObject);
        }
        return dbList;
    }

    private void put(Operator operator, Object object, DBObject baseObject)
    {
        boolean isForUnit = true;
        if (operator instanceof MOperator._Operator)
        {
            MOperator._Operator _operator = (MOperator._Operator) operator;
            isForUnit = _operator.isForUnit();
        } else if (operator == NOT || operator == OR)
        {
            isForUnit = false;
        }

        if (isForUnit)
        {
            dealNormal(operator, object, baseObject);
        } else
        {
            deal2(operator, object, baseObject);

        }
    }

    private void deal2(Operator operator, Object object, DBObject baseObject)
    {
        if (!(object instanceof MongoCondition))
        {
            throw new ConditionException("value should be type of " + getClass()
                    + ".current type is "
                    + object.getClass());
        }
        MongoCondition condition = (MongoCondition) object;

        if (operator == NOT)
        {
            baseObject.put("$not", condition.toFinalObject());
        } else if (operator == OR)
        {
            baseObject.put("$or", condition.toDBList());
        } else if (operator instanceof MOperator._Operator)
        {
            baseObject.put(operator.toString(), condition.toFinalObject());
        } else
        {
            throw new ConditionException("the operator should be _Operator, " + NOT
                    + " or "
                    + OR
                    + " for value of Condition type");
        }

    }

    /**
     * @param unit
     * @Key注解的处理
     */
    private void dealNames(Unit unit)
    {
        if (dealNamesClass == null)
        {
            return;
        }
        if (!unit.isParam1Value())
        {
            unit.setParam1(BaseEasier.dealWith_Key(dealNamesClass, unit.getParam1()));
        }
        if (!unit.isParam2Value())
        {
            unit.setParam2(BaseEasier.dealWith_Key(dealNamesClass, (String) unit.getParam2()));
        }
    }

    private void dealNormal(Operator operator, Object object, DBObject baseObject)
    {
        if (!(object instanceof Unit))
        {
            throw new ConditionException(operator + "-Unit is accept!");
        }
        Unit unit = (Unit) object;

        dealNames(unit);// @Key注解的处理

        Object object2 = unit.getParam2();
        if (object2 != null && (object2 instanceof MongoCondition))
        {
            object2 = ((MongoCondition) object2).toFinalObject();
        }

        if (operator == IN || operator == NIN)
        {
            BasicDBList dbList = new BasicDBList();
            if (object2 instanceof Object[])
            {
                Object[] objects = (Object[]) object2;

                if (objects != null)
                {
                    for (Object v : dbList)
                    {
                        dbList.add(v);
                    }
                }

            } else
            {
                dbList.add(object2);
            }
            baseObject.put(unit.getParam1(), new BasicDBObject(operator == IN ? "$in" : "$nin", dbList));
            return;

        } else if (operator == SUBSTR && object2 != null)
        {
            Class<?> c = object2.getClass();
            baseObject.put(unit.getParam1(), new BasicDBObject("$regex", c.isPrimitive() ? object2
                    : object2 + ""));
            return;
        } else if (operator == EQ)
        {
            baseObject.put(unit.getParam1(), object2);
            return;
        }

        //BasicDBObject basicDBObject = new BasicDBObject(unit.getParam1(), object2);
        String op;
        if (operator == GT)
        {
            op = "$gt";
            // dbObject.put("$gt", basicDBObject);
        } else if (operator == GTE)
        {
            op = "$gte";
            //dbObject.put("$gte", basicDBObject);
        } else if (operator == LT)
        {
            op = "$lt";
            //dbObject.put("$lt", basicDBObject);
        } else if (operator == LTE)
        {
            op = "$lte";
            // dbObject.put("$lte", basicDBObject);
        } else if (operator == NE)
        {
            op = "$ne";
            //dbObject.put("$ne", basicDBObject);
        } else if (operator instanceof MOperator._Operator)
        {
            op = operator.toString();
            // dbObject.put(operator.toString(), basicDBObject);
        } else
        {
            throw new ConditionException("unknown operator " + operator);
        }
        if (baseObject.containsField(unit.getParam1()))
        {
            ((DBObject) baseObject.get(unit.getParam1())).put(op, object2);
        } else
        {
            baseObject.put(unit.getParam1(), new BasicDBObject(op, object2));
        }


    }

    @Override
    public void dealNames(Class<?> c)
    {
        this.dealNamesClass = c;
    }
}
