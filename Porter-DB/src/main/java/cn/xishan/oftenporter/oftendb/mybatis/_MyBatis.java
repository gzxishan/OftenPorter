package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisParams;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/10.
 */
class _MyBatis
{

    private static final Logger LOGGER = LogUtil.logger(_MyBatis.class);

    MyBatisMapper.Type type;
    String resourceDir;
    String name;
    String path;
    Class<?> daoClass;
    String daoAlias;
    String entityAlias;
    Class<?> entityClass;

    boolean isAutoAlias;

    private Map<String, Object> xmlParamsMap;
    private MSqlSessionFactoryBuilder.FileListener fileListener;
    private List<String> paths;

    public _MyBatis(MyBatisMapper.Type type, String resourceDir, String name)
    {
        if (resourceDir != null && !resourceDir.endsWith("/"))
        {
            resourceDir += "/";
        }
        this.type = type;
        this.resourceDir = resourceDir;
        this.name = name;
    }


    public void setFileListener(MSqlSessionFactoryBuilder.FileListener fileListener) throws Exception
    {
        this.fileListener = fileListener;
        if (paths != null)
        {
            List<File> files = getRelatedFile(paths);
            fileListener.onGetFiles(files.toArray(new File[0]));
            paths = null;
        }
    }

    public void init(String[] params)
    {
        Map<String, Object> map = new HashMap<>();
        if (!entityClass.equals(MyBatisMapper.class))
        {
            map.put("entity", entityClass.getSimpleName());
            map.put("entityClass", entityClass.getName());
        }
        map.put("mapperDao", daoClass.getSimpleName());
        map.put("mapperDaoClass", daoClass.getName());

        MyBatisParams myBatisParams = AnnoUtil.getAnnotation(daoClass, MyBatisParams.class);
        if (WPTool.notNullAndEmpty(params) || myBatisParams != null)
        {
            if (params != null)
            {
                for (String param : params)
                {
                    JSONObject jsonObject = JSON.parseObject(param);
                    if (jsonObject != null)
                    {
                        map.putAll(jsonObject);
                    }
                }
            }
            if (myBatisParams != null)
            {
                String[] vals = myBatisParams.value();
                for (String str : vals)
                {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject != null)
                    {
                        map.putAll(jsonObject);
                    }
                }
            }
        }

        this.xmlParamsMap = map;
    }

    public String replaceSqlParams(String sql) throws Exception
    {

        int loopCount = 0;
        Map<String, Object> localParams = new HashMap<>(xmlParamsMap);
        if (paths == null)
        {
            paths = new ArrayList<>();
        } else
        {
            paths.clear();
        }
        while (true)
        {
            if (loopCount > 1000)
            {
                throw new RuntimeException("too much replace for " + daoClass);
            }
            boolean found = false;

            {
                String keyPrefix = "<!--$json:";
                String keySuffix = "-->";
                do
                {
                    int index = sql.indexOf(keyPrefix);
                    if (index == -1)
                    {
                        break;
                    }
                    int index2 = sql.indexOf(keySuffix, index + keyPrefix.length());
                    if (index2 == -1)
                    {
                        throw new RuntimeException("illegal format:" + sql.substring(index));
                    }
                    JSONObject params = JSON.parseObject(sql.substring(index + keyPrefix.length(), index2));
                    if (params != null)
                    {
                        localParams.putAll(params);
                    }
                    sql = sql.substring(0, index) + sql.substring(index2 + keySuffix.length());
                } while (false);
            }

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
                        int index2 = sql.indexOf(keySuffix, index + keyPrefix.length());
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
                            LOGGER.debug("[{}]load classpath content from:{}", this.path, path);
                            path = PackageUtil.getPackageWithRelative(daoClass, path, "/");
                            if (!path.startsWith("/"))
                            {
                                path = "/" + path;
                            }

                            try
                            {
                                File file = null;
                                if (resourceDir != null)
                                {
                                    String filePath = resourceDir + path;
                                    file = new File(filePath);
                                }
                                if (file != null && file.exists() && file.isFile())
                                {
                                    content = FileTool.getString(file, 1024, "utf-8");
                                } else
                                {
                                    content = FileTool.getString(daoClass.getResourceAsStream(path), 1024, "utf-8");
                                }
                                paths.add("classpath:" + path);
                            } catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        } else
                        {//file:
                            LOGGER.debug("[{}]load file content from:", this.path, path);
                            try
                            {
                                content = FileTool.getString(new FileInputStream(path), 1024, "utf-8");
                                paths.add("file:" + path);
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
                key = "$[" + key + "]";
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

        if (fileListener != null)
        {
            setFileListener(fileListener);
        }

        return sql;
    }

    List<File> getRelatedFile(List<String> paths)
    {
        if (resourceDir == null)
        {
            return new ArrayList<>(0);
        }

        List<File> list = new ArrayList<>(paths.size() + 1);

        for (String path : paths)
        {
            File file = null;
            if (path.startsWith("file:"))
            {
                file = new File(path.substring(5));
            } else if (path.startsWith("classpath:") && resourceDir != null)
            {
                file = new File(resourceDir + path.substring(10));
            }
            if (file != null && file.exists() && file.isFile())
            {
                list.add(file);
            }
        }

        return list;
    }
}
