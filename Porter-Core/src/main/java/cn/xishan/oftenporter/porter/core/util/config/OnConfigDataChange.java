package cn.xishan.oftenporter.porter.core.util.config;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于监听整个json配置变化。
 *
 * @author Created by https://github.com/CLovinr on 2020-11-03.
 */
public interface OnConfigDataChange
{
    void onChange(JSONObject newConfig,JSONObject oldConfig)throws Exception;
}
