package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 默认的参数处理错误的原因的实现。
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public class DefaultFailedReason
{

    static class FailedReasonImpl implements ParamDealt.FailedReason
    {
        private JSONObject jsonObject;
        private String desc;

        public FailedReasonImpl(String desc, JSONObject jsonObject)
        {
            this.desc = desc;
            this.jsonObject = jsonObject;
        }

        @Override
        public String toString()
        {
            return jsonObject.toString();
        }

        @Override
        public JSONObject toJSON()
        {
            return jsonObject;
        }

        @Override
        public String desc()
        {
            return desc;
        }
    }

    /**
     * 缺乏必须参数.
     *
     * @param names
     * @return
     */
    public static ParamDealt.FailedReason lackNecessaryParams(String desc, String... names)
    {
        JSONObject json = new JSONObject(2);
        json.put("type", "lack");
        JSONArray jsonArray = new JSONArray(names.length);
        json.put("names", jsonArray);
        for (int i = 0; i < names.length; i++)
        {
            jsonArray.add(names[i]);
        }
        return new FailedReasonImpl(desc, json);
    }

    public static ParamDealt.FailedReason parsePortInObjException(String desc)
    {
        JSONObject json = new JSONObject(2);
        json.put("type", "ex_inObj");
        return new FailedReasonImpl(desc, json);
    }

    /**
     * 参数类型不合法。
     *
     * @param names
     * @return
     */
    public static ParamDealt.FailedReason illegalParams(String desc, String... names)
    {
        JSONObject json = new JSONObject(2);
        json.put("type", "illegal");
        JSONArray jsonArray = new JSONArray(names.length);
        json.put("names", jsonArray);
        for (int i = 0; i < names.length; i++)
        {
            jsonArray.add(names[i]);
        }
        return new FailedReasonImpl(desc, json);
    }


}
