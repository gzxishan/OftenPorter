package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.AutoSetDealtForDBSource;
import cn.xishan.oftenporter.oftendb.data.DBHandleSource;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.script.*;
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
    private static int count=0;


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

    private String getScript(Object object, Field field, String option) throws IOException
    {
        String path;
        if (WPTool.isEmpty(jDaoOption.classpath))
        {
            path = "/" + object.getClass().getPackage().getName().replace('.', '/');
        } else
        {
            path = jDaoOption.classpath;

        }
        if (!path.endsWith("/"))
        {
            path += "/";
        }
        path += object.getClass().getSimpleName() + ".js";
        return FileTool.getString(object.getClass().getResourceAsStream(path), 1024, jDaoOption.scriptEncoding);
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        synchronized (JDaoGen.class){
            if(count==0&&scriptEngineManager!=null){
                scriptEngineManager=null;
            }
        }
    }



    @Override
    public  Object genObject(Object object, Field field, String option)
    {
        synchronized (JDaoGen.class){
            try
            {
                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
                SimpleBindings bindings = new SimpleBindings();
                bindings.put("jdaoBridge",new JsInterface(jDaoOption.tableNamePrefix));

                scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                String script = getScript(object, field, option);
                scriptEngine.eval(script);

                AutoSetDealtForDBSource.setUnit(object,dbSource);
                DBHandleSource handleSource = dbSource.getDBHandleSource();
                JDaoImpl jDao = new JDaoImpl(new JsBridge((Invocable) scriptEngine, (SqlSource) handleSource));
                count++;
                return jDao;
            } catch (Exception e)
            {
                throw new JInitException(e);
            }
        }
    }
}
