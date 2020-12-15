package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import cn.xishan.oftenporter.servlet.websocket.IContainerResource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class OftenServletResponse extends LocalResponse
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServletResponse.class);

    private class HttpServletResponseWrapperImpl extends HttpServletResponseWrapper implements IContainerResource<HttpServletResponse>
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
            request.setAttribute(OftenServletResponse.class.getName(), "1");
            return super.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException
        {
            hasGotStreamType = 2;
            request.setAttribute(OftenServletResponse.class.getName(), "2");
            return super.getWriter();
        }

        @Override
        public HttpServletResponse containerRes(HttpServletResponse res)
        {
            return (HttpServletResponse) getResponse();
        }
    }

    private HttpServletRequest request;
    private HttpServletResponseWrapperImpl response;

    OftenServletResponse(HttpServletRequest request, HttpServletResponse response)
    {
        super(null);
        this.request = request;
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
        } else if (response.isCommitted())
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("already committed");
            }
        } else
        {
            request.setAttribute(OftenServletResponse.class.getName(), "0");
            try (PrintWriter printWriter = response.getWriter())
            {
                response.hasGotStreamType = 2;
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
