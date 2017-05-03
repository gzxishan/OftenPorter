package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import com.alibaba.fastjson.JSONObject;

import javax.script.Invocable;
import java.io.File;

/**
 * Created by chenyg on 2017-05-02.
 */
class JsBridgeOfDebug extends JsBridge
{
    private File jsFile;
    private JDaoOption jDaoOption;
    private long lasttime;

    public JsBridgeOfDebug(JDaoOption jDaoOption, String jsFile, DBSource dbSource, SqlSource sqlSource)
    {
        super(null,dbSource, sqlSource);
        this.jsFile = new File(jsFile);
        this.jDaoOption = jDaoOption;
        loadJsEngine();
    }

    private void loadJsEngine()
    {
        File file = jsFile;
        if (lasttime != file.lastModified())
        {
            try
            {
                invocable = JDaoGen
                        .getJsInvocable(FileTool.getString(file, 1024, jDaoOption.scriptEncoding), jDaoOption);
                lasttime=file.lastModified();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T> T query(String method, JSONObject json, WObject wObject)
    {
        loadJsEngine();
        return super.query(method, json, wObject);
    }

    @Override
    public <T> T execute(String method, JSONObject json, WObject wObject)
    {
        loadJsEngine();
        return super.execute(method, json, wObject);
    }
}
