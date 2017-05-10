package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.JDaoPath;
import cn.xishan.oftenporter.oftendb.data.AutoSetDealtForDBSource;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-29.
 */
class JDaoGen implements AutoSetGen
{
    @AutoSet
    JDaoOption jDaoOption;

    @AutoSet
    DBSource dbSource;

    private static ScriptEngineManager scriptEngineManager;
    private static int count = 0;


    public JDaoGen()
    {
        synchronized (JDaoGen.class)
        {

            try
            {
                if (scriptEngineManager == null)
                {
                    scriptEngineManager = new ScriptEngineManager();
                }
            } catch (Exception e)
            {
                throw new JInitException(e);
            }

        }
    }

    private String getScript(Object object, JDaoPath jDaoPath) throws IOException
    {
        String path = jDaoOption.classpath;
        boolean needAddFileName = true;
        if (jDaoPath != null)
        {
            if (!jDaoPath.value().equals(""))
            {
                path = jDaoPath.value().startsWith("/") ? jDaoPath.value() : "/" + PackageUtil
                        .getPackageWithRelative(object.getClass(), jDaoPath.value(), "/");
                needAddFileName = false;
            }
        }
        if (WPTool.isEmpty(path))
        {
            path = "/" + object.getClass().getPackage().getName().replace('.', '/');
        }
        if (needAddFileName)
        {
            if (!path.endsWith("/"))
            {
                path += "/";
            }
            path += object.getClass().getSimpleName() + ".js";
        }
        return FileTool.getString(object.getClass().getResourceAsStream(path), 1024, jDaoOption.scriptEncoding);
    }

    static void doFinalize()
    {
        synchronized (JDaoGen.class)
        {
            if (count == 0 && scriptEngineManager != null)
            {
                scriptEngineManager = null;
            }
        }
    }

    static Invocable getJsInvocable(String script, JDaoOption jDaoOption) throws Exception
    {
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("jdaoBridge", new JsInterface(jDaoOption.tableNamePrefix));
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptEngine.eval(script);
        return (Invocable) scriptEngine;
    }

    @Override
    public Object genObject(Object object, Field field, String option)
    {
        synchronized (JDaoGen.class)
        {
            try
            {
                JsBridge jsBridge;
                SqlSource sqlSource = (SqlSource) dbSource.getDBHandleSource();
                JDaoPath jDaoPath = field.getAnnotation(JDaoPath.class);
                if (jDaoOption.debugDirPath != null && jDaoPath == null)
                {
                    String dir = jDaoOption.debugDirPath.replace('\\', '/');
                    if (!dir.endsWith("/"))
                    {
                        dir += "/";
                    }
                    jsBridge = new JsBridgeOfDebug(jDaoOption, dir + object.getClass().getSimpleName() + ".js",
                            dbSource, sqlSource);
                } else
                {
                    jsBridge = new JsBridge(getJsInvocable(getScript(object, jDaoPath), jDaoOption), dbSource,
                            sqlSource);
                }
                AutoSetDealtForDBSource.setUnit(object, dbSource);
                JDaoImpl jDao = new JDaoImpl(jsBridge);
                count++;
                return jDao;
            } catch (Exception e)
            {
                throw new JInitException(e);
            }
        }
    }
}
