package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisParams;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/10.
 */
class _MyBatis
{
    MyBatis.Type type;
    String dir;
    String name;
    Class<?> daoClass;
    String daoAlias;
    String entityAlias;
    Class<?> entityClass;

    boolean isAutoAlias;

    private Map<String, Object> xmlParamsMap;

    public _MyBatis(MyBatis.Type type, String dir, String name)
    {
        this.type = type;
        this.dir = dir;
        this.name = name;
    }

    public void init(String params)
    {
        Map<String, Object> map = new HashMap<>();

        MyBatisParams myBatisParams = AnnoUtil.getAnnotation(daoClass, MyBatisParams.class);
        if (WPTool.notNullAndEmpty(params) || myBatisParams != null)
        {
            if (params != null)
            {
                JSONObject jsonObject = JSON.parseObject(params);
                if (jsonObject != null)
                {
                    map.putAll(jsonObject);
                }
            }
            if (myBatisParams != null)
            {
                JSONObject jsonObject = JSON.parseObject(myBatisParams.value());
                if (jsonObject != null)
                {
                    map.putAll(jsonObject);
                }
            }
        }

        this.xmlParamsMap = map;
    }

    public int sizeOfXmlParams()
    {
        return xmlParamsMap.size();
    }

    public String replaceSqlParams(String sql)
    {
        if (xmlParamsMap.size() == 0)
        {
            return sql;
        }

        for (Map.Entry<String, Object> entry : xmlParamsMap.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null)
            {
                continue;
            }
            key = "${" + key + "}";
            StringBuilder stringBuilder = new StringBuilder();
            while (true)
            {
                int index = sql.indexOf(key);
                if (index == -1)
                {
                    break;
                }
                stringBuilder.append(sql.substring(0, index));
                stringBuilder.append(String.valueOf(value));
                sql = sql.substring(index + key.length());
            }
            stringBuilder.append(sql);
            sql = stringBuilder.toString();
        }

        return sql;
    }
}
