package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.JDaoPath;
import cn.xishan.oftenporter.oftendb.data.AutoSetDealtForDBSource;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
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
                throw new JDaoInitException(e);
            }

        }
    }

    static class Path
    {
        String path;
        boolean isFile;
    }

    private Path getPath(JDaoPath jDaoPath,Class<?> currentObjectClass, Field field)
    {
        boolean isFile = WPTool.notNullAndEmpty(jDaoOption.debugDirPath);
        String optionDir = isFile ? jDaoOption.debugDirPath : jDaoOption.classpath;
        if (WPTool.isEmpty(optionDir))
        {
            optionDir = "/" + currentObjectClass.getPackage().getName().replace('.', '/');
        }

        String name;
        if (jDaoPath != null)
        {
            String path = jDaoPath.value();
            if (path.equals(""))
            {
                path = jDaoPath.path();
            }
            if (jDaoPath.relativeToOptionPath())
            {
                boolean start = optionDir.startsWith("/");
                optionDir = PackageUtil.getPathWithRelative('/', optionDir, path, "/");
                if (start && !optionDir.startsWith("/"))
                {
                    optionDir = "/" + optionDir;
                }
            } else
            {
                optionDir = path;
            }

            if (jDaoPath.fieldName())
            {
                name = field.getName() + ".js";
            } else
            {
                name = jDaoPath.name().equals("") ? currentObjectClass.getSimpleName() + ".js" : jDaoPath.name();
            }

        } else
        {
            name = currentObjectClass.getSimpleName() + ".js";
        }
        if (optionDir.length() > 0 && !optionDir.endsWith("/"))
        {
            optionDir += "/";
        }
        Path path = new Path();
        path.isFile = isFile;
        path.path = optionDir + name;
        return path;
    }

    private String getScript(Class<?> currentObjectClass,String path,Field field) throws IOException
    {
        InputStream inputStream = currentObjectClass.getResourceAsStream(path);
        String script = inputStream == null ? null : FileTool.getString(inputStream, 1024, jDaoOption.scriptEncoding);
        if (script == null)
        {
            throw new RuntimeException("script not found in classpath:" + path+"("+currentObjectClass+" "+field.getName() +")");
        }
        return script;
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

    static Invocable getJsInvocable(String script, DBSource dbSource, JDaoOption jDaoOption) throws Exception
    {
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("js");
        if (jDaoOption.injectScript != null)
        {
            tryCompileScript(scriptEngine, jDaoOption.injectScript);
        }
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("jdaoBridge", new _JsInterface(dbSource, jDaoOption.tableNamePrefix));
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        tryCompileScript(scriptEngine, script);
        return (Invocable) scriptEngine;
    }

    private static void tryCompileScript(ScriptEngine scriptEngine, String script) throws ScriptException
    {
        if (scriptEngine instanceof Compilable)
        {
            Compilable compilable = (Compilable) scriptEngine;
            try
            {
                CompiledScript compiledScript = compilable.compile(script);
                compiledScript.eval();
            } catch (ScriptException e)
            {
                scriptEngine.eval(script);
            }
        } else
        {
            scriptEngine.eval(script);
        }
    }

    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field, String option)
    {
        synchronized (JDaoGen.class)
        {
            try
            {
                JsBridge jsBridge;
                SqlSource sqlSource = (SqlSource) dbSource;
                JDaoPath jDaoPath = field.getAnnotation(JDaoPath.class);

                Path path = getPath(jDaoPath, currentObjectClass, field);
                Logger logger = LogUtil.logger(_SqlSorce.class);
                if (path.isFile)
                {
                    jsBridge = new JsBridgeOfDebug(jDaoOption, path.path, dbSource, sqlSource, logger);
                } else
                {
                    jsBridge = new JsBridge(getJsInvocable(getScript(currentObjectClass, path.path,field), dbSource, jDaoOption),
                            dbSource, sqlSource, path.path, logger);
                }
                AutoSetDealtForDBSource.setUnit(currentObject, dbSource);
                JDaoImpl jDao = new JDaoImpl(jsBridge);
                count++;
                return jDao;
            } catch (Exception e)
            {
                throw new JDaoInitException(e);
            }
        }
    }
}
