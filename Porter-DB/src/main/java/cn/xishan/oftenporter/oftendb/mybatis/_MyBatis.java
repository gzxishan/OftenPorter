package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatis;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisParams;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/10.
 */
class _MyBatis
{

    private static final Logger LOGGER = LogUtil.logger(_MyBatis.class);

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

        int loopCount = 0;
        Map<String, Object> localParams = new HashMap<>(xmlParamsMap);
        while (true)
        {
            if (loopCount > 1000)
            {
                throw new RuntimeException("too much replace for " + daoClass);
            }
            boolean found = false;
            {

                for (int i = 0; i < 2; i++)
                {
                    do
                    {
                        String keyPrefix;
                        String keySuffix = "-->";

                        if (i == 0)
                        {//classpath:
                            keyPrefix = "<!--$classpath:";
                        } else
                        {//file:
                            keyPrefix = "<!--$file:";
                        }
                        int index = sql.indexOf(keyPrefix);
                        if (index == -1)
                        {
                            break;
                        }
                        int index2 = sql.indexOf(keySuffix, index);
                        if (index2 == -1)
                        {
                            throw new RuntimeException("illegal format:" + sql.substring(index));
                        }
                        found = true;
                        String content;
                        String path = sql.substring(index + keyPrefix.length(), index2);
                        int indexX = path.indexOf("!");
                        if (indexX > 0)
                        {
                            JSONObject params = JSON.parseObject(path.substring(indexX + 1));
                            path = path.substring(0, indexX);
                            if (params != null)
                            {
                                localParams.putAll(params);
                            }
                        }
                        path = path.trim();
                        if (i == 0)
                        {//classpath:
                            LOGGER.debug("load classpath content from:"+path);
                            path = PackageUtil.getPackageWithRelative(daoClass, path, "/");
                            if (!path.startsWith("/"))
                            {
                                path = "/" + path;
                            }

                            try
                            {
                                content = FileTool.getString(daoClass.getResourceAsStream(path), 1024, "utf-8");
                            } catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        } else
                        {//file:
                            LOGGER.debug("load file content from:"+path);
                            try
                            {
                                content = FileTool.getString(new FileInputStream(path), 1024, "utf-8");
                            } catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }

                        sql = sql.substring(0, index) +
                                content +
                                sql.substring(index2 + keySuffix.length());
                    } while (false);
                }

            }

            for (Map.Entry<String, Object> entry : localParams.entrySet())
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
                    found = true;
                    stringBuilder.append(sql.substring(0, index));
                    stringBuilder.append(String.valueOf(value));
                    sql = sql.substring(index + key.length());
                }
                stringBuilder.append(sql);
                sql = stringBuilder.toString();
            }

            if (!found)
            {
                break;
            } else
            {
                loopCount++;
            }
        }

        return sql;
    }
}
