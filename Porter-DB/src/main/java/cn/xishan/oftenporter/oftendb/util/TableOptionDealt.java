package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.annotation.TableOptionFilter;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;
import java.util.*;


/**
 * @author Created by https://github.com/CLovinr on 2018/1/18.
 */
class TableOptionDealt implements BindEntityDealt.IHandle<TableOption>
{

    private boolean hasConf = false;
    private String[] queryContains = {};
    private String[] queryArrayContains = {};
    private String[] settingsContains = {};
    private String[] orderContains = {};
    private Map<String, String> replaceKeyMap;
    private boolean disableEmptyContains = false;
    private TableOption.IHandle handle;

    @Override
    public void init(String option, Method method) throws Exception
    {
        TableOptionFilter tableOptionFilter = AnnoUtil.getAnnotation(method, TableOptionFilter.class);
        initConfig(tableOptionFilter);
    }

    @Override
    public void init(String option, Class<?> clazz) throws Exception
    {
        TableOptionFilter tableOptionFilter = AnnoUtil.getAnnotation(clazz, TableOptionFilter.class);
        initConfig(tableOptionFilter);
    }

    private void initConfig(TableOptionFilter tableOptionFilter) throws Exception
    {
        handle = TableOption.getDefaultHandle();
        if (tableOptionFilter == null)
        {
            return;
        }
        hasConf = true;
        disableEmptyContains = tableOptionFilter.disableEmptyContains();
        queryContains = tableOptionFilter.queryContains();
        queryArrayContains = tableOptionFilter.queryArrayContains();
        settingsContains = tableOptionFilter.settingsContains();
        orderContains = tableOptionFilter.orderContains();
        if (!TableOption.IHandle.class.equals(tableOptionFilter.handle()))
        {
            handle = OftenTool.newObject(tableOptionFilter.handle());
        }

        Arrays.sort(queryContains);
        Arrays.sort(queryArrayContains);


        List<String> settings = new ArrayList<>();
        OftenTool.addAll(settings, settingsContains);
        settings.add("skip");
        settings.add("limit");
        settings.add("order");
        settingsContains = settings.toArray(OftenTool.EMPTY_STRING_ARRAY);
        Arrays.sort(settingsContains);
        Arrays.sort(orderContains);

        String[] replaceKey = tableOptionFilter.replaceKey();
        if (replaceKey.length > 0)
        {
            replaceKeyMap = new HashMap<>(replaceKey.length);
        }
        for (String replace : replaceKey)
        {
            int index = replace.indexOf("=");
            replaceKeyMap.put(replace.substring(0, index), replace.substring(index + 1));
        }

    }


    @Override
    public Object deal(OftenObject oftenObject, Porter porter, TableOption object) throws Exception
    {
        return deal(oftenObject, object);
    }

    @Override
    public Object deal(OftenObject oftenObject, PorterOfFun fun, TableOption object) throws Exception
    {
        return deal(oftenObject, object);
    }


    private final Object deal(OftenObject oftenObject, TableOption tableOption) throws Exception
    {
        if (handle != null)
        {
            handle.handle(oftenObject, tableOption);
        }

        if (!hasConf)
        {
            return tableOption;
        }

        replaceKey(tableOption);

        Object obj = check(queryContains, tableOption.query, "query");
        if (obj != null)
        {
            return obj;
        }

        if (tableOption.queryArray != null && queryArrayContains != null)
        {
            for (int i = 0; i < tableOption.queryArray.size(); i += 2)
            {
                String key = tableOption.queryArray.getString(i);
                if (Arrays.binarySearch(queryArrayContains, key) < 0)
                {
                    obj = DefaultFailedReason.parseOftenEntitiesException("queryArray could not contains:" + key);
                    break;
                }
            }
        }

        if (obj != null)
        {
            return obj;
        }

        obj = check(settingsContains, tableOption.settings, "settings");
        if (obj != null)
        {
            return obj;
        }
        Object order = tableOption.settings.get("order");
        if (order != null && orderContains != null)
        {
            if (order instanceof JSONArray)
            {
                JSONArray jsonArray = (JSONArray) order;
                for (int i = 0; i < jsonArray.size(); i += 2)
                {
                    String key = jsonArray.getString(i);
                    if (Arrays.binarySearch(orderContains, key) < 0)
                    {
                        obj = DefaultFailedReason.parseOftenEntitiesException("order could not contains:" + key);
                        break;
                    }
                }
            } else if (order instanceof JSONObject)
            {
                obj = check(orderContains, (Map<String, Object>) order, "order");
            }
        }
        if (obj != null)
        {
            return obj;
        }

        return tableOption;
    }


    private void replaceJson(Set<String> replaceSet, JSONObject json)
    {
        if (json == null)
        {
            return;
        }
        replaceSet.clear();
        for (String key : json.keySet())
        {
            if (replaceKeyMap.containsKey(key))
            {
                replaceSet.add(key);
            }
        }
        for (String key : replaceSet)
        {
            Object value = json.get(key);
            json.put(replaceKeyMap.get(key), value);
        }
    }

    private void replaceKey(TableOption tableOption)
    {
        if (replaceKeyMap == null)
        {
            return;
        }
        Set<String> replaceSet = new HashSet<>(8);
        replaceJson(replaceSet, tableOption.query);
        if (tableOption.settings != null)
        {
            Object order = tableOption.settings.get("order");
            if (order instanceof JSONObject)
            {
                replaceJson(replaceSet, (JSONObject) order);
            } else
            {
                JSONArray jsonArray = (JSONArray) order;
                for (int i = 0; i < jsonArray.size(); i += 2)
                {
                    String key = jsonArray.getString(i);
                    if (replaceKeyMap.containsKey(key))
                    {
                        jsonArray.set(i, replaceKeyMap.get(key));
                    }
                }
            }
        }
        if (tableOption.queryArray != null)
        {
            for (int i = 0; i < tableOption.queryArray.size(); i++)
            {
                replaceJson(replaceSet, tableOption.queryArray.getJSONObject(i));
            }
        }
    }

    private Object check(String[] contains, Map<String, Object> map, String attr)
    {
        if (contains.length == 0 && disableEmptyContains)
        {
            return null;
        }
        if (map == null)
        {
            map = Collections.emptyMap();
        }
        for (String key : map.keySet())
        {
            if (Arrays.binarySearch(contains, key) < 0)
            {
                return DefaultFailedReason.parseOftenEntitiesException(attr + " could not contains:" + key);
            }
        }
        return null;
    }

}
