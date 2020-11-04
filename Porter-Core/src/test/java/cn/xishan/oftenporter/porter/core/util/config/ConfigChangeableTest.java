package cn.xishan.oftenporter.porter.core.util.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ConfigChangeableTest
{
    @Test
    public void testBase()
    {
        JSONObject json = JSON.parseObject("{a:'A',b:{c:128},d:'D'}");
        JSONObject newJson = JSON.parseObject("{a:'A2',b:{c:256},d:'D'}");

        ConfigChangeable configChangeable = new ConfigChangeable();
        boolean[] rs = {
                false, false,
                false,
        };
        configChangeable.addOnConfigValueChange(String.class,
                (attr, newValue, oldValue) -> rs[0] = attr.equals("a") && !newValue.equals(oldValue), "a");

        configChangeable.addOnConfigValueChange(String.class,
                (attr, newValue, oldValue) -> rs[1] = attr.equals("d"), "d");

        configChangeable.addOnConfigValueChange(Integer.class, new OnConfigValueChange<Integer>()
        {
            @Override
            public void onChange(String attr, Integer newValue, Integer oldValue) throws Exception
            {
                rs[2] = attr.equals("b.c") && !newValue.equals(oldValue);
            }
        }, "b.c");

        configChangeable.submitChange(newJson, json);

        assertTrue(rs[0]);
        assertFalse(rs[1]);
        assertTrue(rs[2]);

    }
}