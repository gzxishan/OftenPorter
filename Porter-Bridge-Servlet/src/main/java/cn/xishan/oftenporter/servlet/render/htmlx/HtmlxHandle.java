package cn.xishan.oftenporter.servlet.render.htmlx;


import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.HttpCacheUtil;
import cn.xishan.oftenporter.servlet.OftenServlet;
import cn.xishan.oftenporter.servlet.render.RenderPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxHandle extends AspectOperationOfPortIn.HandleAdapter<Htmlx>
{
    private String[] path;
    private String oftenPath;
    private String encoding;
    private String contentType;
    private int cacheSeconds;
    private String index;

    private String title;
    private String description;
    private String keywords;


    @Override
    public boolean init(Htmlx current, IConfigData configData, PorterOfFun porterOfFun)
    {
        if (current.enable())
        {
            this.path = current.path();
            this.encoding = current.encoding();
            this.contentType = current.contentType();
            this.cacheSeconds = current.cacheSeconds();
            this.index = current.index();
            this.title = current.title();
            this.description = current.description();
            this.keywords = current.keywords();
            this.oftenPath = porterOfFun.getPath();
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public void onStart(OftenObject oftenObject)
    {
        ServletContext servletContext = oftenObject.getContextSet(ServletContext.class);
        OftenServlet oftenServlet = oftenObject.getContextSet(OftenServlet.class);
        ServletRegistration.Dynamic dynamic = servletContext
                .addServlet(oftenPath, new HtmlxServlet(oftenServlet, oftenPath));
        dynamic.addMapping(path);
        dynamic.setAsyncSupported(true);
    }

    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Throwable
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        HttpServletResponse response = oftenObject.getRequest().getOriginalResponse();

        String rpath = request.getRequestURI().substring(request.getContextPath().length());
        if (rpath.endsWith("/"))
        {
            rpath += this.index;
        }
        ServletContext servletContext = request.getServletContext();
        String filePath = servletContext.getRealPath(rpath);

        File file = new File(filePath);
        if (!file.exists())
        {
            response.sendError(404);
            return null;
        }

        long lastModified=file.lastModified();

        if(HttpCacheUtil.isCacheIneffectiveWithModified(lastModified,request,response)){
            Document document = Jsoup.parse(file, encoding);
            HtmlxDoc htmlxDoc = new HtmlxDoc(document);

            htmlxDoc.setCacheSeconds(cacheSeconds);
            htmlxDoc.setContentType(contentType);
            htmlxDoc.setEncoding(encoding);

            if (OftenTool.notEmpty(title))
            {
                htmlxDoc.title(title);
            }

            if (OftenTool.notEmpty(description))
            {
                htmlxDoc.setMetaDescription(description);
            }

            if (OftenTool.notEmpty(keywords))
            {
                htmlxDoc.setMetaKeywords(keywords);
            }

            Object rs = porterOfFun.invokeByHandleArgs(oftenObject, htmlxDoc);
            response.setContentType(htmlxDoc.getContentType());
            response.setCharacterEncoding(htmlxDoc.getEncoding());
            if (htmlxDoc.getCacheSeconds() > 0)
            {
                HttpCacheUtil.setCacheWithModified(htmlxDoc.getCacheSeconds(), lastModified, response);
            }
            String htmlStr = document.outerHtml();
            if (rs instanceof RenderPage || rs instanceof Map)
            {
                Map<String, ?> map;
                if (rs instanceof RenderPage)
                {
                    map = ((RenderPage) rs).getData();
                } else
                {
                    map = (Map) rs;
                }
                htmlStr = DealSharpProperties.replaceSharpProperties(htmlStr, map);

            } else if (rs != null)
            {
                throw new RuntimeException("unknown return:" + rs);
            }

            byte[] bytes = htmlStr.getBytes(Charset.forName(htmlxDoc.getEncoding()));
            response.setContentLength(bytes.length);
            try (OutputStream os = response.getOutputStream())
            {
                os.write(bytes);
                os.flush();
            }
        }
        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }

    @Override
    public PortMethod[] getMethods()
    {
        return new PortMethod[]{PortMethod.GET};
    }
}
