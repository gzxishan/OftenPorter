package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class OftenServletResponse extends LocalResponse
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServletResponse.class);

    static class HttpServletResponseWrapperImpl extends HttpServletResponseWrapper
    {

        int hasGotStreamType = 0;

        public HttpServletResponseWrapperImpl(HttpServletResponse response)
        {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException
        {
            hasGotStreamType = 1;
            return super.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException
        {
            hasGotStreamType = 2;
            return super.getWriter();
        }
    }

    private HttpServletResponseWrapperImpl response;

    OftenServletResponse(HttpServletResponse response)
    {
        super(null);
        this.response = new HttpServletResponseWrapperImpl(response);
    }

    @Override
    public void doClose(Object writeObject) throws IOException
    {
        if (response.hasGotStreamType != 0)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("already invoked:HttpServletResponse.{}",
                        response.hasGotStreamType == 1 ? "getOutputStream()" : "getWriter()");
            }
        } else
        {
            try (PrintWriter printWriter = response.getWriter())
            {
                setContentType(writeObject);
                printWriter.print(writeObject);
                printWriter.flush();
            }
        }
    }

    public void setContentType(Object object)
    {
        String ctype = response.getContentType();
        if (OftenTool.isEmpty(ctype))
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
