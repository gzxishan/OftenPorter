package cn.xishan.oftenporter.porter.core.base;


import com.alibaba.fastjson.JSONObject;

public interface INameValues
{

    /**
     * 得到键名或字段名列表。
     *
     * @return 键名或字段名列表
     */
    String[] getNames();

    /**
     * 得到键名或字段值列表。
     *
     * @return 键名或字段值列表
     */
    Object[] getValues();

    JSONObject toJSON();

}
