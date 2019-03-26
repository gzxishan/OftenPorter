package cn.xishan.oftenporter.servlet.render.htmlx;


import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.HttpCacheUtil;
import cn.xishan.oftenporter.servlet.OftenServlet;
import cn.xishan.oftenporter.servlet.render.RenderPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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
    private byte[] otherwiseHtml;
    private byte[] otherwisePage;
    private String otherwisePagePath;
    private String otherwisePageEncoding;
    private String htmlSuffix;

    private long otherwiseLastmodified = System.currentTimeMillis();


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
            this.otherwiseHtml = current.otherwiseHtml().getBytes(Charset.forName(this.encoding));
            this.otherwisePagePath = current.otherwisePage();
            this.otherwisePageEncoding = current.otherwisePageEncoding();
            this.oftenPath = porterOfFun.getPath();
            this.htmlSuffix = current.htmlSuffix();

            if (this.encoding.equals(""))
            {
                this.encoding = "utf-8";
            }

            if (this.otherwisePageEncoding.equals(""))
            {
                this.otherwisePageEncoding = this.encoding;
            }

            if (this.htmlSuffix.equals(""))
            {
                this.htmlSuffix = "html";
            }

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
        String name = OftenStrUtil.getNameFormPath(rpath);

        ServletContext servletContext = request.getServletContext();
        String filePath = servletContext.getRealPath(rpath);

        File file = new File(filePath);
        long lastModified;
        if (!file.exists())
        {
            lastModified = -1;
        } else
        {
            lastModified = file.lastModified();
        }


        if (HttpCacheUtil.isCacheIneffectiveWithModified(lastModified, request, response))
        {
            Document document;
            HtmlxDoc.PageType pageType;
            if (file.exists())
            {
                if (!OftenStrUtil.getSuffix(name).equals(htmlSuffix))
                {//非html文件
                    HttpCacheUtil.setCacheWithModified(cacheSeconds, lastModified, response);
                    response.setContentLength((int) file.length());
                    response.setCharacterEncoding(encoding);
                    String type = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(name);
                    response.setContentType(type);
                    try (OutputStream os = response.getOutputStream())
                    {
                        FileTool.file2out(file, os, 2048);
                        os.flush();
                    }
                    return null;
                }
                document = Jsoup.parse(file, encoding);
                pageType = HtmlxDoc.PageType.Normal;
            } else
            {
                if (otherwisePage == null)
                {
                    if (OftenTool.notEmpty(otherwisePagePath))
                    {
                        synchronized (this)
                        {
                            if (OftenTool.notEmpty(otherwisePagePath))
                            {
                                String path = servletContext.getRealPath(otherwisePagePath);
                                otherwisePagePath = null;
                                if (path != null)
                                {
                                    File ofile = new File(path);
                                    if (ofile.exists())
                                    {
                                        otherwiseLastmodified = ofile.lastModified();
                                        otherwisePage = FileTool.getData(ofile, 1024);
                                    }
                                }
                            }
                        }
                    }
                }

                if (otherwisePage == null)
                {
                    InputStream in = new ByteArrayInputStream(otherwiseHtml);
                    document = Jsoup.parse(in, encoding, "");
                    pageType = HtmlxDoc.PageType.OthersizeHtml;
                } else
                {
                    InputStream in = new ByteArrayInputStream(otherwisePage);
                    document = Jsoup.parse(in, otherwisePageEncoding, "");
                    pageType = HtmlxDoc.PageType.OtherwisePage;
                }
                lastModified = otherwiseLastmodified;
            }
            HtmlxDoc htmlxDoc = new HtmlxDoc(document, pageType);

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
            if (!htmlxDoc.isBreak())
            {
                response.setContentType(htmlxDoc.getContentType());
                response.setCharacterEncoding(htmlxDoc.getEncoding());
                if (htmlxDoc.willCache() && htmlxDoc.getCacheSeconds() > 0)
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

        } else
        {
            if (cacheSeconds > 0)
            {
                HttpCacheUtil.setCacheWithModified(cacheSeconds, lastModified, response);
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
