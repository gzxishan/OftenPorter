package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.ExceptColumns;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisParams;
import cn.xishan.oftenporter.oftendb.util.DataUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/10.
 */
@ExceptColumns
class _MyBatis
{

    static class Alias
    {
        String alias;
        Class<?> type;

        public Alias(String alias, Class<?> type)
        {
            this.alias = alias;
            this.type = type;
        }
    }

    private static final Logger LOGGER = LogUtil.logger(_MyBatis.class);
    private static final ExceptColumns DEFAULT_EXCEPT_COLUMNS = AnnoUtil
            .getAnnotation(_MyBatis.class, ExceptColumns.class);

    MyBatisMapper.Type type;
    String resourceDir;
    String name;
    private String path, parentDir;
    Class<?> daoClass;
    Class<?> entityClass;
    Alias[] aliases;

    boolean isAutoAlias;

    private Map<String, Object> xmlParamsMap;
    MSqlSessionFactoryBuilder builder;
    private MSqlSessionFactoryBuilder.FileListener fileListener;
    private List<String> paths;
    private String columnCoverString;
    private String tableName;

    public _MyBatis(Alias[] aliases, MyBatisMapper.Type type, String columnCoverString, String resourceDir, String name)
    {
        this.aliases = aliases;
        if (resourceDir != null && !resourceDir.endsWith("/"))
        {
            resourceDir += "/";
        }
        this.type = type;
        this.columnCoverString = columnCoverString;
        this.resourceDir = resourceDir;
        this.name = name;
    }


    public void setPath(String path)
    {
        this.path = path;
        parentDir = PackageUtil.getPathWithRelative(path, "./");
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
        if (OftenTool.notNullAndEmpty(params) || myBatisParams != null)
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

    private void putAllNotOverride(Map<String, Object> from, Map<String, Object> to)
    {
        if (from == null)
        {
            return;
        }
        for (Map.Entry<String, Object> entry : from.entrySet())
        {
            if (to.containsKey(entry.getKey()))
            {
                continue;
            }
            to.put(entry.getKey(), entry.getValue());
        }
    }

    private boolean isTRUE(Map<String, Object> _localParams, String varName)
    {
        varName = varName.trim();
        boolean isNot = false;
        if (varName.startsWith("!"))
        {
            isNot = true;
            varName = varName.substring(1).trim();
        }
        boolean is = true;
        if (varName.equals(""))
        {
            is = false;
        } else
        {
            Object object = _localParams.get(varName);
            if (OftenTool.isEmpty(object))
            {
                is = false;
            } else if (object instanceof Boolean)
            {
                is = (Boolean) object;
            } else if (object instanceof Number)
            {
                is = ((Number) object).doubleValue() != 0;
            } else if (object instanceof CharSequence || object instanceof Character)
            {
                String str = String.valueOf(object).trim();
                is = !"".equals(str) && !"0".equals(str);
            }
        }

        return isNot != is;
    }

    public String replaceSqlParams(String _sql) throws Exception
    {
        StringBuilder sqlBuilder = new StringBuilder(_sql);
        int loopCount = 0;
        Map<String, Object> _localParams = new HashMap<>(xmlParamsMap);
        if (paths == null)
        {
            paths = new ArrayList<>();
        } else
        {
            paths.clear();
        }
        while (true)
        {
            if (loopCount > 5000)
            {
                throw new RuntimeException("too much replace for " + daoClass);
            }
            boolean found = false;

            {
                String keyPrefix = "<!--$json:";
                String keySuffix = "-->";
                do
                {
                    int index = sqlBuilder.indexOf(keyPrefix);
                    if (index == -1)
                    {
                        break;
                    }
                    int index2 = sqlBuilder.indexOf(keySuffix, index + keyPrefix.length());
                    if (index2 == -1)
                    {
                        throw new RuntimeException("illegal format:" + sqlBuilder.substring(index));
                    }
                    JSONObject params = JSON.parseObject(sqlBuilder.substring(index + keyPrefix.length(), index2));
                    putAllNotOverride(params, _localParams);
                    sqlBuilder.delete(index, index2 + keySuffix.length());
                } while (false);
            }

            {

                for (int i = 0; i <= 2; i++)
                {
                    do
                    {
                        String keyPrefix;
                        String keySuffix = "-->";

                        if (i == 0)
                        {//classpath:
                            keyPrefix = "<!--$classpath:";
                        } else if (i == 1)
                        {
                            keyPrefix = "<!--$path:";
                        } else
                        {//file:
                            keyPrefix = "<!--$file:";
                        }
                        int index = sqlBuilder.indexOf(keyPrefix);
                        if (index == -1)
                        {
                            break;
                        }
                        int index2 = sqlBuilder.indexOf(keySuffix, index + keyPrefix.length());
                        if (index2 == -1)
                        {
                            throw new RuntimeException("illegal format:" + sqlBuilder.substring(index));
                        }
                        found = true;
                        String content;
                        String path = sqlBuilder.substring(index + keyPrefix.length(), index2);
                        int indexX = path.indexOf("!");
                        if (indexX > 0)
                        {
                            JSONObject params = JSON.parseObject(path.substring(indexX + 1));
                            path = path.substring(0, indexX);
                            putAllNotOverride(params, _localParams);
                        }
                        path = path.trim();
                        if (i == 0 || i == 1)
                        {//classpath,path
                            if (i == 0)
                            {
                                path = PackageUtil.getPackageWithRelative(daoClass, path, '/');
                            } else
                            {
                                path = PackageUtil.getPathWithRelative(this.parentDir, path);
                            }
                            LOGGER.debug("[{}]load classpath content from:{}", this.path, path);
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
                                    InputStream in = ResourceUtil.getAbsoluteResourceStream(path);
                                    if (in == null)
                                    {
                                        throw new RuntimeException("not found:" + path);
                                    }
                                    content = FileTool.getString(in, 1024, "utf-8");
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
                        sqlBuilder.replace(index, index2 + keySuffix.length(), content);
                    } while (false);
                }

            }

            if (!found)
            {
                break;
            } else
            {
                loopCount++;
            }
        }

        {//处理$enable
            String keyPrefix = "<!--$enable:";
            String keySuffix = "-->";
            while (true)
            {
                int startStart = sqlBuilder.indexOf(keyPrefix);
                if (startStart == -1)
                {
                    break;
                }
                int startEnd = sqlBuilder.indexOf(keySuffix, startStart + keyPrefix.length());
                if (startEnd == -1)
                {
                    throw new InitException("expected '-->' for:" + keyPrefix);
                }
                String varName = sqlBuilder.substring(startStart + keyPrefix.length(), startEnd).trim();
                startEnd += keySuffix.length();

                Pattern pattern = Pattern.compile("<!--\\$enable-end:\\s*" + varName + "\\s*-->");
                Matcher matcher = pattern.matcher(sqlBuilder);
                int endStart, endEnd;
                if (matcher.find())
                {
                    endStart = matcher.start();
                    endEnd = matcher.end();
                    if (endStart <= startEnd)
                    {
                        throw new InitException("illegal position:<!--$enable-end:" + varName + "-->");
                    }
                } else
                {
                    throw new InitException("expected:<!--$enable-end:" + varName + "-->");
                }

                sqlBuilder.delete(endStart, endEnd);//删除结束标记
                if (!isTRUE(_localParams, varName))
                {
                    sqlBuilder.delete(startEnd, endStart);//删除中间内容
                }
                sqlBuilder.delete(startStart, startEnd);//删除开始标记
            }
        }

        {//$[varName]变量处理,不存在的替换成空字符串
            Pattern VAR_PATTERN = Pattern.compile("\\$\\[([a-zA-Z0-9\\-_$]+)\\]");
            while (true)
            {
                Matcher matcher = VAR_PATTERN.matcher(sqlBuilder);
                if (!matcher.find())
                {
                    break;
                }
                int index = matcher.start();
                int index2 = matcher.end();
                String varName = matcher.group(1);
                Object value = _localParams.get(varName);
                sqlBuilder.replace(index, index2, value == null ? "" : String.valueOf(value));
            }
        }

        {//处理<!--$set-table:name=tableName-->
            Pattern TABLE_NAME_PATTERN = Pattern.compile("<!--\\$set-table:\\s*name\\s*=\\s*([a-zA-Z0-9_]+)\\s*-->");
            Matcher matcher = TABLE_NAME_PATTERN.matcher(sqlBuilder);
            while (matcher.find())
            {
                String tableName = matcher.group(1);
                this.tableName = tableName;
                LOGGER.info("set table name:{}", tableName);
            }
        }

        {//处理select,insert与update

            if (sqlBuilder.indexOf("$[select-part:") >= 0
                    || sqlBuilder.indexOf("$[insert-part:") >= 0
                    || sqlBuilder.indexOf("$[update-part:") >= 0
            )
            {

                List<String> dbColumnsOfCurrent = getDBColumns(entityClass);
                List<String> refColumnsOfCurrent = getRefColumns(entityClass);

                while (true)
                {
                    String tag = "$[select-part:";
                    int index = sqlBuilder.indexOf(tag);
                    if (index == -1)
                    {
                        break;
                    }
                    int index2 = sqlBuilder.indexOf("]", index + 1);
                    if (index2 == -1)
                    {
                        throw new InitException("expected ']' for:" + tag);
                    }

                    String[] excepts = getExcepts(ExceptPartType.SELECT, sqlBuilder, tag, index, index2);

                    String tname = "";

                    {
                        Pattern tnamePattern = Pattern.compile("tname[\\s]*=[\\s]*([a-zA-Z0-9_]*)");
                        Matcher matcher = tnamePattern.matcher(sqlBuilder.substring(index + tag.length(), index2));
                        if (matcher.find())
                        {
                            tname = matcher.group(1);
                        }
                    }

                    List<String> dbColumns = getDBColumns(dbColumnsOfCurrent, sqlBuilder, tag, index, index2);

                    List<String> _dbColumns = dbColumns;
                    if (excepts.length > 0)
                    {
                        _dbColumns = new ArrayList<>();

                        for (int i = 0; i < dbColumns.size(); i++)
                        {
                            if (Arrays.binarySearch(excepts, dbColumns.get(i)) >= 0)
                            {
                                continue;
                            }
                            if (OftenTool.isEmpty(tname))
                            {
                                _dbColumns.add(dbColumns.get(i));
                            } else
                            {
                                _dbColumns.add(tname + "." + dbColumns.get(i));
                            }
                        }
                    }
                    String selectPart = OftenStrUtil.join(",", _dbColumns);
                    sqlBuilder.replace(index, index2 + 1, selectPart);
                }

                while (true)
                {
                    String tag = "$[insert-part:";
                    int index = sqlBuilder.indexOf(tag);
                    if (index == -1)
                    {
                        break;
                    }
                    int index2 = sqlBuilder.indexOf("]", index + 1);
                    if (index2 == -1)
                    {
                        throw new InitException("expected ']' for:" + tag);
                    }

                    String[] excepts = getExcepts(ExceptPartType.INSERT, sqlBuilder, tag, index, index2);

                    List<String> dbColumns = dbColumnsOfCurrent;

                    List<String> _dbColumns = dbColumns;
                    List<String> _refColumns = refColumnsOfCurrent;
                    if (excepts.length > 0)
                    {
                        _dbColumns = new ArrayList<>();
                        _refColumns = new ArrayList<>();
                        for (int i = 0; i < dbColumns.size(); i++)
                        {
                            if (Arrays.binarySearch(excepts, dbColumns.get(i)) >= 0)
                            {
                                continue;
                            }
                            _dbColumns.add(dbColumns.get(i));
                            _refColumns.add(refColumnsOfCurrent.get(i));
                        }
                    }
                    String insertPart = "(" + OftenStrUtil.join(",", _dbColumns) + ") VALUES (" + OftenStrUtil
                            .join(",", _refColumns) + ")";
                    sqlBuilder.replace(index, index2 + 1, insertPart);
                    LOGGER.debug("{} insert-part={}",tableName,insertPart);
                }

                while (true)
                {

                    String tag = "$[update-part:";
                    int index = sqlBuilder.indexOf(tag);
                    if (index == -1)
                    {
                        break;
                    }
                    int index2 = sqlBuilder.indexOf("]", index + 1);
                    if (index2 == -1)
                    {
                        throw new InitException("expected ']' for:" + tag);
                    }


                    String[] excepts = getExcepts(ExceptPartType.UPDATE, sqlBuilder, tag, index, index2);

                    List<String> dbColumns = dbColumnsOfCurrent;

                    List<String> _dbColumns = dbColumns;
                    List<String> _refColumns = refColumnsOfCurrent;
                    if (excepts.length > 0)
                    {
                        _dbColumns = new ArrayList<>();
                        _refColumns = new ArrayList<>();
                        for (int i = 0; i < dbColumns.size(); i++)
                        {
                            if (Arrays.binarySearch(excepts, dbColumns.get(i)) >= 0)
                            {
                                continue;
                            }
                            _dbColumns.add(dbColumns.get(i));
                            _refColumns.add(refColumnsOfCurrent.get(i));
                        }
                    }

                    List<String> list = new ArrayList<>(_dbColumns.size());
                    for (int i = 0; i < _dbColumns.size(); i++)
                    {
                        list.add(_dbColumns.get(i) + "=" + _refColumns.get(i));
                    }
                    String updatePart = OftenStrUtil.join(",", list);
                    sqlBuilder.replace(index, index2 + 1, updatePart);
                    LOGGER.debug("{} update-part={}",tableName,updatePart);
                }

            }
        }

        if (fileListener != null)
        {
            setFileListener(fileListener);
        }
        return sqlBuilder.toString();
    }

    enum ExceptPartType
    {
        SELECT, INSERT, UPDATE
    }


    private List<String> getRefColumns(Class entityClass)
    {
        Set<String> realColumnsSet = builder.getTableColumns(tableName);
        List<String> refColumns = new ArrayList<>();
        Field[] _fields = OftenTool.getAllFields(entityClass);
        for (Field field : _fields)
        {
            String columnName = DataUtil.getTiedName(field);
            if (columnName != null && (realColumnsSet.isEmpty() || realColumnsSet.contains(columnName)))
            {
                refColumns.add("#{" + field.getName() + "}");
            }
        }
        return refColumns;
    }

    private List<String> getDBColumns(Class entityClass)
    {
        Set<String> realColumnsSet = builder.getTableColumns(tableName);
        List<String> dbColumns = new ArrayList<>();
        Field[] _fields = OftenTool.getAllFields(entityClass);
        for (Field field : _fields)
        {
            String columnName = DataUtil.getTiedName(field);
            if (columnName != null && (realColumnsSet.isEmpty() || realColumnsSet.contains(columnName)))
            {
                dbColumns.add(columnCoverString + columnName + columnCoverString);
            }
        }
        return dbColumns;
    }

    private List<String> getDBColumns(List<String> dbColumnsOfCurrent, StringBuilder sqlBuilder, String tag, int index,
            int index2) throws ClassNotFoundException
    {
        Pattern entityPattern = Pattern.compile("entityClass[\\s]*=[\\s]*([a-zA-Z0-9_.$]+)");
        Matcher matcher = entityPattern.matcher(sqlBuilder.substring(index + tag.length(), index2));
        if (matcher.find())
        {
            String entityClassName = matcher.group(1);
            return getDBColumns(PackageUtil.newClass(entityClassName, null));
        } else
        {
            return dbColumnsOfCurrent;
        }
    }

    private String[] getExcepts(ExceptPartType exceptPartType, StringBuilder sqlBuilder, String tag, int index,
            int index2)
    {
        Pattern exceptPattern = Pattern.compile("except=\\{([a-zA-Z0-9_,\\s]*)\\}");
        ExceptColumns exceptColumns = AnnoUtil.getAnnotation(entityClass, ExceptColumns.class);
        if (exceptColumns == null)
        {
            exceptColumns = DEFAULT_EXCEPT_COLUMNS;
        }

        String[] excepts = null;
        if (exceptColumns.enableXmlConfiged())
        {
            Matcher matcher = exceptPattern.matcher(sqlBuilder.substring(index + tag.length(), index2));
            if (matcher.find())
            {
                excepts = OftenStrUtil.split(matcher.group(1).trim(), ",");
            } else
            {
                excepts = OftenTool.EMPTY_STRING_ARRAY;
            }

        } else
        {
            excepts = OftenTool.EMPTY_STRING_ARRAY;
        }

        List<String> list = new ArrayList<>();
        OftenTool.addAll(list, excepts);
        OftenTool.addAll(list, exceptColumns.fields());
        switch (exceptPartType)
        {

            case SELECT:
                OftenTool.addAll(list, exceptColumns.selectPart());
                break;
            case INSERT:
                OftenTool.addAll(list, exceptColumns.insertPart());
                break;
            case UPDATE:
                OftenTool.addAll(list, exceptColumns.updatePart());
                break;
        }

        excepts = list.toArray(OftenTool.EMPTY_STRING_ARRAY);
        for (int i = 0; i < excepts.length; i++)
        {
            excepts[i] = columnCoverString + excepts[i].trim() + columnCoverString;
        }
        Arrays.sort(excepts);
        return excepts;
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
