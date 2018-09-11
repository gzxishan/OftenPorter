package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class WServletResponse extends LocalResponse
{

    private HttpServletResponse response;
    private String encoding;

    WServletResponse(HttpServletResponse response)
    {
        super(null);
        this.response = response;
    }

    void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    @Override
    public void doClose(Object writeObject) throws IOException
    {
        setContentType(writeObject);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(writeObject);
        printWriter.flush();
        printWriter.close();
    }

    public void setContentType(Object object)
    {
        String ctype = response.getContentType();
        if (WPTool.isEmpty(ctype))
        {

            if (object != null && ((object instanceof JResponse) || (object instanceof JSONObject) ||
                    (object instanceof JSONArray) || (object instanceof JSONHeader)))
            {

                response.setContentType(ContentType.APP_JSON.getType());
            } else
            {
                response.setContentType(ContentType.TEXT_PLAIN.getType());
            }

        }
    }
}
