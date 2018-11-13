package cn.xishan.oftenporter.oftendb.db;

import cn.xishan.oftenporter.porter.core.base.INameValues;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用来说明哪些字段有效或无效。
 *
 * @author ZhuiFeng
 */
public class DBNameValues implements INameValues
{

    public interface Foreach {
        /**
         * @param name
         * @param value
         * @return 返回false表示中断循环.
         */
        public boolean forEach(String name, Object value);
    }

    // private Map<String, Object> map;
    private List<String> names;
    private List<Object> values;
    private boolean filterNullAndEmpty = false;
    private Set<String> filterNullKeys;

    public DBNameValues() {
        names = new ArrayList<>();
        values = new ArrayList<>();
    }

    public DBNameValues(String name, Object value) {
        this();
        append(name, value);
    }

    public DBNameValues(int capacity) {
        names = new ArrayList<String>(capacity);
        values = new ArrayList<Object>(capacity);
    }

    /**
     * 设置或清除待过滤的空属性.
     *
     * @param filterNullKeys
     */
    public DBNameValues setFilterNullKeys(String... filterNullKeys) {
        if (filterNullKeys.length == 0) {
            this.filterNullKeys = null;
        } else {
            Set<String> set = new HashSet<>();
            for (String s : filterNullKeys) {
                set.add(s);
            }
            this.filterNullKeys=set;
        }
        return this;
    }

    /**
     * 是否过滤掉值为null或""的元素,默认为false,请在{@linkplain #append(String, Object)}之前调用。
     *
     * @param filterNullAndEmpty
     * @return
     */
    public DBNameValues filterNullAndEmpty(boolean filterNullAndEmpty) {
        this.filterNullAndEmpty = filterNullAndEmpty;
        return this;
    }

    /**
     * 添加键值对。
     *
     * @param name
     * @param value
     * @return
     */
    public DBNameValues append(String name, Object value) {
        if (!(filterNullAndEmpty && (filterNullKeys == null || filterNullKeys.contains(name))) || OftenTool.notNullAndEmpty(value)) {
            names.add(name);
            values.add(value);
        }
        return this;
    }


    public String[] names() {
        return names.toArray(new String[0]);
    }

    public Object[] values() {
        return values.toArray(new Object[0]);
    }

    public void forEach(Foreach foreach) {
        for (int i = 0; i < names.size(); i++) {
            if (!foreach.forEach(names.get(i), values.get(i))) {
                break;
            }
        }

    }

    public void clear() {
        names.clear();
        values.clear();
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < names.size(); i++) {
            jsonObject.put(names.get(i), values.get(i));
        }
        return jsonObject;
    }

    public Object value(int index) {
        return values.get(index);
    }

    public String name(int index) {
        return names.get(index);
    }

    public int size() {
        return names.size();
    }

    @Override
    public String[] getNames() {
        return names();
    }

    @Override
    public Object[] getValues() {
        return values();
    }

}