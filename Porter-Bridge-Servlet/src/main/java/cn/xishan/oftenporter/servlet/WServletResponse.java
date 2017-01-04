package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.WResponse;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class WServletResponse implements WResponse
{

    private HttpServletResponse response;

    WServletResponse(HttpServletResponse response)
    {
        this.response = response;
    }


    public HttpServletResponse getServletResponse()
    {
        return response;
    }


    public void sendRedirect(String redirect) throws IOException
    {
        response.sendRedirect(redirect);
    }

    @Override
    public void write(@NotNull Object object) throws IOException
    {

        setContentType(object);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(object);
        printWriter.flush();
    }

    @Override
    public void close() throws IOException
    {
        response.getWriter().close();
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
