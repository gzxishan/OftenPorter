package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.Unit;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 用于动态的、无需声明类的情况。
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class DataDynamic extends DataAble {
    private JSONObject jsonObject;
    private String[] keyNames;
    private String[] dbNames;

    /**
     * 构建一个动态的。其字段是根据必须参数与非必须参数中不为空的来动态确定。
     *
     */
    public DataDynamic() {
    }

    /**
     * 构建一个指定了字段的。
     *
     * @param dbNames        数据库字段名称，与keyNames一一对应；若为null，则表示与keyNames相同。
     * @param keyNames       field名称
     */
    public DataDynamic( String[] dbNames, String... keyNames) {
        this();
        if (dbNames == null) {
            Arrays.sort(keyNames);
            this.keyNames = keyNames;
            this.dbNames = keyNames;
        } else {
            Map<String, String> map = new HashMap<>(dbNames.length);
            for (int i = 0; i < dbNames.length; i++) {
                map.put(keyNames[i], dbNames[i]);
            }
            Arrays.sort(keyNames);
            for (int i = 0; i < dbNames.length; i++) {
                dbNames[i] = map.get(keyNames[i]);
            }
            this.dbNames = dbNames;
            this.keyNames = keyNames;
        }

    }


    public void setForQuery(Condition forQuery) {
    }

    @Override
    public Condition forQuery() {
        return null;
    }

    public void setKeys(KeysSelection keys) {
    }

    @Override
    public KeysSelection keys() {
        return null;
    }

    @Override
    public JSONObject toJsonObject() {
        return jsonObject;
    }

    @Override
    protected NameValues toNameValues(ParamsGetter.Params params) throws Exception {
        NameValues nameValues = new NameValues(jsonObject.size());
        if (jsonObject != null) {
            Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                nameValues.put(entry.getKey(), entry.getValue());
            }
        }
        return nameValues;
    }


    @Override
    public void whenSetDataFinished(SetType setType, int optionCode, WObject wObject,
                                    DBHandleAccess dbHandleAccess) throws DataException {

    }

    private boolean isInKeyNames(String name) {
        if (keyNames == null || Arrays.binarySearch(keyNames, name) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    private void setValue(JSONObject jsonObject, InNames.Name[] names, Object[] values) throws JSONException {
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i].varName;
                if (values[i] != null && isInKeyNames(name)) {
                    jsonObject.put(name, values[i]);
                }
            }
        }
    }

    @Override
    protected void setParams(InNames.Name[] neceFields, Object[] nvalues, InNames.Name[] unneceFields,
                             Object[] uvalues, InNames.Name[] innerNames, Object[] inners) throws Exception {
        jsonObject = new JSONObject();
        setValue(jsonObject, neceFields, nvalues);
        setValue(jsonObject, unneceFields, uvalues);
        setValue(jsonObject, innerNames, inners);
    }

    private String dbName(int index) {
        return dbNames[index];
    }

    private String dbName(String name) {
        if (dbNames != null) {
            int index = Arrays.binarySearch(keyNames, name);
            name = dbNames[index];
        }
        return name;
    }

    @Override
    protected String[] getFinalKeys(KeysSelection keysSelection, ParamsGetter.Params params) {
        String[] keys = null;
        if (keysSelection != null) {
            if (keysSelection.isSelect) {
                keys = keysSelection.keys;
                // 转换成数据库的名称
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = dbName(keys[i]);
                }

            } else {
                String[] unKeys = keysSelection.keys;
                List<String> list = new ArrayList<String>();
                String[] names = keyNames;
                for (int i = 0; i < names.length; i++) {
                    if (DataUtil.indexOf(unKeys, names[i]) == -1) {
                        list.add(dbName(i));
                    }
                }
                keys = list.toArray(new String[0]);
            }
        }
        return keys;
    }

    @Override
    protected void dealNames(Condition condition) {

    }

    @Override
    protected void dealNames(QuerySettings querySettings) {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected DataAble cloneData() {
        try {
            DataDynamic dataAble = (DataDynamic) clone();
            dataAble.jsonObject = null;
            return dataAble;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WObject wObject,
                                 ParamsGetter.Params params) {
        Condition condition = null;

        if (selection != null) {
            condition = dbHandleSource.newCondition();
            boolean toNull;
            int[] nIndexes = selection.nIndexes;

            if (nIndexes != null) {
                InNames.Name[] fnNames = wObject.fInNames.nece;
                Object[] cns = wObject.fn;

                for (int i = 0; i < nIndexes.length; i++) {
                    int index = nIndexes[i];
                    if (index < 0) {
                        index = -(index + 1);
                        toNull = true;
                    } else {
                        toNull = false;
                    }
                    if (cns[index] != null) {
                        String sname = fnNames[index].varName;

                        condition.put(Condition.EQ, new Unit(sname, cns[index]));
                        if (toNull) {
                            cns[index] = null;
                        }
                    }

                }

            }

            int[] uIndexes = selection.uIndexes;

            if (uIndexes != null) {
                InNames.Name[] fnNames = wObject.fInNames.unece;
                Object[] cus = wObject.fu;

                for (int i = 0; i < uIndexes.length; i++) {
                    int index = uIndexes[i];
                    if (index < 0) {
                        index = -(index + 1);
                        toNull = true;
                    } else {
                        toNull = false;
                    }
                    if (cus[index] != null) {
                        String sname = fnNames[index].varName;
                        condition.put(Condition.EQ, new Unit(sname, cus[index]));
                        if (toNull) {
                            cus[index] = null;
                        }
                    }

                }

            }

        }
        return condition;
    }
}
