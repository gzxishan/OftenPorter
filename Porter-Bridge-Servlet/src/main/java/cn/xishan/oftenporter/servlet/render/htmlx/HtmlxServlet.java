package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.servlet.OftenServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxServlet extends HttpServlet
{
    private OftenServlet oftenServlet;
    private String oftenPath;

    HtmlxServlet(OftenServlet oftenServlet, String oftenPath)
    {
        this.oftenServlet = oftenServlet;
        this.oftenPath = oftenPath;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        oftenServlet.doRequest(req, oftenPath, resp, PortMethod.GET);
    }
}
