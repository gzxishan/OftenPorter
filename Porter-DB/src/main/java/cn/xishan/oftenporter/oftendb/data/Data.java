package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.Unit;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 父类的相关注解变量(public)在子类中仍然有效。
 *
 * @author ZhuiFeng
 */
public abstract class Data extends DataAble
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Data.class);

    private static class ClassAndField
    {
        private String[] names;
        private Field[] fields;
        private String[] tiedNames;

        public ClassAndField(Class<?> clazz) throws NoSuchFieldException
        {
            Field[] fields = clazz.getFields();
            String[] names = new String[fields.length];
            for (int i = 0; i < names.length; i++)
            {
                names[i] = fields[i].getName();
            }
            Arrays.sort(names);
            List<String> nameList = new ArrayList<>();
            List<Field> fieldList = new ArrayList<>();
            List<String> tiedNameList = new ArrayList<>();
            for (int i = 0; i < names.length; i++)
            {
                Field f = clazz.getField(names[i]);
                String tiedName = DataUtil.getTiedName(f);
                if (tiedName!=null)
                {
                    f.setAccessible(true);
                    nameList.add(names[i]);
                    fieldList.add(f);
                    tiedNameList.add(tiedName);
                }
            }
            this.names = nameList.toArray(new String[0]);
            this.fields = fieldList.toArray(new Field[0]);
            this.tiedNames = tiedNameList.toArray(new String[0]);
        }

        public Field[] getFields()
        {
            return fields;
        }

        public String[] getNames()
        {
            return names;
        }

        public String tiedName(String fieldName)
        {
            int index = Arrays.binarySearch(names, fieldName);
            return index >= 0 ? tiedNames[index] : null;
        }

        public String tiedName(int index)
        {
            return tiedNames[index];
        }

        public Field byIndex(int index)
        {
            return fields[index];
        }

        public Field byName(String fieldName)
        {
            int index = Arrays.binarySearch(names, fieldName);
            Field f = index >= 0 ? fields[index] : null;
            return f;
        }
    }

    private static final Map<Class<?>, ClassAndField> classFields = new ConcurrentHashMap<>();

    public Data()
    {
        try
        {
            classFields.put(getClass(), new ClassAndField(getClass()));
        } catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * 转换成json对象.
     *
     * @return 若为null表示转换出现失败。
     */
    @Override
    public JSONObject toJsonObject()
    {
        JSONObject jsonObject = null;
        try
        {
            ClassAndField cf = getClassAndField();
            Field[] fs = cf.getFields();
            String[] names = cf.names;

            JSONObject json = new JSONObject(fs.length);

            for (int i = 0; i < fs.length; i++)
            {
                json.put(names[i], fs[i].get(this));
            }

            jsonObject = json;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        return jsonObject;
    }


    private void setValue(ClassAndField cf, InNames.Name[] names, Object[] values) throws
            IllegalAccessException
    {
        if (names != null && names.length > 0)
        {
            for (int i = 0; i < names.length; i++)
            {
                if (values[i] != null)
                {
                    Field field = cf.byName(names[i].varName);
                    if (field != null)
                    {
                        field.set(this, values[i]);
                    }
                }
            }
        }
    }

    /**
     * 设置值.
     *
     * @param neceFields   必需参数名
     * @param nvalues      为null的不会设置.
     * @param unneceFields 非必需参数名
     * @param uvalues      非必需参数，为null的不会设置.
     * @throws Exception
     */
    @Override
    protected final void setParams(InNames.Name[] neceFields, Object[] nvalues, InNames.Name[] unneceFields,
            Object[] uvalues, InNames.Name[] innerNames, Object[] inners) throws Exception
    {
        ClassAndField cf = getClassAndField();
        setValue(cf, neceFields, nvalues);
        setValue(cf, unneceFields, uvalues);
        setValue(cf, innerNames, inners);
    }

    private ClassAndField getClassAndField()
    {
        ClassAndField cf = classFields.get(getClass());
        return cf;
    }


    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }


    private Condition queryCondition;
    private KeysSelection keysSelection;

    public void setForKeys(KeysSelection keysSelection)
    {
        this.keysSelection = keysSelection;
    }

    public void setForQuery(Condition queryCondition)
    {
        this.queryCondition = queryCondition;
    }

    /**
     * 用于数据库修改、查询或删除时的寻找条件
     *
     * @return
     */
    public Condition forQuery()
    {
        return queryCondition;
    }

    public KeysSelection keys()
    {
        return keysSelection;
    }

    /**
     * <pre>
     * 设置类变量完成时、且在进行数据库调用前调用此函数.
     * 对于{@linkplain SetType#QUERY}和{@linkplain SetType#DELETE}不会设置值
     * ({@linkplain #setParams(InNames.Name[], Object[], InNames.Name[], Object[], InNames.Name[], Object[])})
     * ，但会调用本函数。
     * </pre>
     *
     * @param setType
     * @param optionCode     可选code
     * @param wObject
     * @param dbHandleAccess
     * @throws DataException 若抛出异常，则向客户端响应失败。
     */
    public abstract void whenSetDataFinished(SetType setType, int optionCode, WObject wObject,
            DBHandleAccess dbHandleAccess) throws DataException;


    @Override
    protected final NameValues toNameValues(ParamsGetter.Params params) throws Exception
    {

        ClassAndField cf = getClassAndField();
        Field[] fields = cf.getFields();
        NameValues nameValues = new NameValues(fields.length);
        for (int i = 0; i < fields.length; i++)
        {
            nameValues.put(cf.tiedName(i), fields[i].get(this));
        }

        return nameValues;
    }


    @Override
    protected String[] getFinalKeys(KeysSelection keysSelection,
            ParamsGetter.Params params)
    {
        String[] keys = null;
        if (keysSelection != null)
        {
            ClassAndField cf = getClassAndField();
            if (keysSelection.isSelect)
            {
                keys = keysSelection.keys;
                // 转换成数据库的名称
                for (int i = 0; i < keys.length; i++)
                {
                    keys[i] = cf.tiedName(keys[i]);
                }

            } else
            {
                String[] unKeys = keysSelection.keys;
                List<String> list = new ArrayList<String>();
                Field[] fields = cf.getFields();
                String[] names = cf.getNames();
                for (int i = 0; i < fields.length; i++)
                {
                    String name = names[i];
                    if (DataUtil.indexOf(unKeys, name) == -1)
                    {
                        list.add(cf.tiedName(i));
                    }
                }
                keys = list.toArray(new String[0]);
            }
        }
        return keys;
    }

    @Override
    protected final void dealNames(Condition condition)
    {
        condition.dealNames(getClass());
    }

    @Override
    protected final void dealNames(QuerySettings querySettings)
    {
        querySettings._dealNames(getClass());
    }

    protected final Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WObject wObject,
            ParamsGetter.Params params)
    {
        Condition condition = null;

        if (selection != null)
        {
            condition = dbHandleSource.newCondition();
            boolean toNull;
            int[] nIndexes = selection.nIndexes;

            ClassAndField cf = getClassAndField();
            if (nIndexes != null)
            {
                InNames.Name[] fnNames = wObject.fInNames.nece;
                Object[] fn = wObject.fn;

                for (int i = 0; i < nIndexes.length; i++)
                {
                    int index = nIndexes[i];
                    if (index < 0)
                    {
                        index = -(index + 1);
                        toNull = true;
                    } else
                    {
                        toNull = false;
                    }
                    if (fn[index] != null)
                    {
                        String sname = cf.tiedName(fnNames[index].varName);

                        condition.put(Condition.EQ, new Unit(sname, fn[index]));
                        if (toNull)
                        {
                            fn[index] = null;
                        }
                    }

                }

            }

            int[] uIndexes = selection.uIndexes;

            if (uIndexes != null)
            {
                InNames.Name[] fuNames = wObject.fInNames.unece;
                Object[] fu = wObject.fu;

                for (int i = 0; i < uIndexes.length; i++)
                {
                    int index = uIndexes[i];
                    if (index < 0)
                    {
                        index = -(index + 1);
                        toNull = true;
                    } else
                    {
                        toNull = false;
                    }
                    if (fu[index] != null)
                    {
                        String sname = cf.tiedName(fuNames[index].varName);
                        condition.put(Condition.EQ, new Unit(sname, fu[index]));
                        if (toNull)
                        {
                            fu[index] = null;
                        }
                    }

                }

            }

        }
        return condition;
    }


    @Override
    protected DataAble cloneData()
    {
        try
        {
            DataAble dataAble = (DataAble) clone();
            return dataAble;
        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
