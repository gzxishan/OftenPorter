package cn.xishan.oftenporter.servlet.render.htmlx;


import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.servlet.HttpCacheUtil;
import cn.xishan.oftenporter.servlet.OftenServlet;
import cn.xishan.oftenporter.servlet.render.RenderPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
@Htmlx(path = "")
public class HtmlxHandle extends AspectOperationOfPortIn.HandleAdapter<Htmlx> implements Comparable<HtmlxHandle>
{
    private static final String HANDLE_KEY = HtmlxHandle.class.getName() + "@-handle-key-";
    @AutoSet
    private ServletContext servletContext;

    private Htmlx defaultHtmlx;
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
    private String[] htmlSuffix;
    private String baseDir;
    private boolean isDebug;
    private int order;

    private long otherwiseLastmodified = -1;


    public String getBaseDir()
    {
        return baseDir;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public String getOtherwisePageEncoding()
    {
        return otherwisePageEncoding;
    }

    @Override
    public boolean init(Htmlx current, IConfigData configData, Porter porter)
    {
        return false;
    }

    private String getValue(String currentValue, String classValue, String defaultValue)
    {
        if (!currentValue.equals(classValue) && !currentValue.equals(defaultValue))
        {//如果当前值不等于类上的、且不是默认值，则使用当前值
            return currentValue;
        } else
        {
            return classValue;
        }
    }


    private String[] getValue(String[] currentValue, String[] classValue, String[] defaultValue)
    {
        if (!OftenStrUtil.equals(currentValue, classValue) && !OftenStrUtil.equals(currentValue, defaultValue))
        {//如果当前值不等于类上的、且不是默认值，则使用当前值
            return currentValue;
        } else
        {
            return classValue;
        }
    }

    private int getValue(int currentValue, int classValue, int defaultValue)
    {
        if (currentValue != classValue && currentValue != defaultValue)
        {//如果当前值不等于类上的、且不是默认值，则使用当前值
            return currentValue;
        } else
        {
            return classValue;
        }
    }

    private boolean getValue(boolean currentValue, boolean classValue, boolean defaultValue)
    {
        if (currentValue != classValue && currentValue != defaultValue)
        {//如果当前值不等于类上的、且不是默认值，则使用当前值
            return currentValue;
        } else
        {
            return classValue;
        }
    }

    @Override
    public boolean init(Htmlx current, IConfigData configData, PorterOfFun porterOfFun)
    {
        if (current.enable())
        {
            Htmlx classHtmlx = AnnoUtil.getAnnotation(porterOfFun.getPorter().getClazz(), Htmlx.class);
            this.defaultHtmlx = AnnoUtil.getAnnotation(HtmlxHandle.class, Htmlx.class);
            if (classHtmlx == null)
            {
                classHtmlx = this.defaultHtmlx;
            }

            this.path = current.path();
            this.baseDir = getValue(current.baseDir(), classHtmlx.baseDir(), defaultHtmlx.baseDir());
            if (!this.baseDir.endsWith("/"))
            {
                this.baseDir += "/";
            }
            this.encoding = getValue(current.encoding(), classHtmlx.encoding(), defaultHtmlx.encoding());
            switch (getValue(current.isDebug(), classHtmlx.isDebug(), defaultHtmlx.isDebug()))
            {
                case "true":
                case "yes":
                case "1":
                    this.isDebug = true;
                    break;
                default:
                    this.isDebug = false;
            }

            this.otherwisePagePath = getValue(current.otherwisePage(), classHtmlx.otherwisePage(),
                    defaultHtmlx.otherwisePage());
            this.otherwisePageEncoding = getValue(current.otherwisePageEncoding(), classHtmlx.otherwisePageEncoding(),
                    defaultHtmlx.otherwisePageEncoding());
            this.otherwiseHtml = getValue(current.otherwiseHtml(), classHtmlx.otherwiseHtml(),
                    defaultHtmlx.otherwiseHtml()).getBytes(Charset.forName(this.encoding));

            this.contentType = getValue(current.contentType(), classHtmlx.contentType(), defaultHtmlx.contentType());
            this.cacheSeconds = getValue(current.cacheSeconds(), classHtmlx.cacheSeconds(),
                    defaultHtmlx.cacheSeconds());
            this.index = getValue(current.index(), classHtmlx.index(), defaultHtmlx.index());
            this.title = getValue(current.title(), classHtmlx.title(), defaultHtmlx.title());
            this.description = getValue(current.description(), classHtmlx.description(), defaultHtmlx.description());
            this.keywords = getValue(current.keywords(), classHtmlx.keywords(), defaultHtmlx.keywords());
            this.oftenPath = porterOfFun.getPath();
            this.htmlSuffix = getValue(current.htmlSuffix(), classHtmlx.htmlSuffix(), defaultHtmlx.htmlSuffix());
            this.order = current.order();

            List<HtmlxHandle> handleList = configData.get(HANDLE_KEY);
            if (handleList == null)
            {
                handleList = new ArrayList<>();
                configData.set(HANDLE_KEY, handleList);
            }
            handleList.add(this);

            if (this.encoding.equals(""))
            {
                this.encoding = "utf-8";
            }

            if (this.otherwisePageEncoding.equals(""))
            {
                this.otherwisePageEncoding = this.encoding;
            }

            for (int i = 0; i < this.path.length; i++)
            {
                this.path[i] = PackageUtil.getPathWithRelative(this.baseDir, this.path[i]);
            }
            if (OftenTool.notEmpty(otherwisePagePath))
            {
                this.otherwisePagePath = PackageUtil.getPathWithRelative(this.baseDir, this.otherwisePagePath);
            }

            if (this.htmlSuffix.length == 0)
            {
                this.htmlSuffix = new String[]{"html"};
            }
            Arrays.sort(this.htmlSuffix);//排序,后面会进行搜索

            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public void onStart(OftenObject oftenObject)
    {
        IConfigData configData = oftenObject.getConfigData();
        List<HtmlxHandle> handleList = configData.get(HANDLE_KEY);
        if (handleList != null)
        {
            configData.remove(HANDLE_KEY);
            OftenServlet oftenServlet = oftenObject.getContextSet(OftenServlet.class);
            HtmlxHandle[] handles = handleList.toArray(new HtmlxHandle[0]);
            Arrays.sort(handles);

            for (HtmlxHandle htmlxHandle : handles)
            {
                String oftenPath = htmlxHandle.oftenPath;
                String[] path = htmlxHandle.path;
                FilterRegistration.Dynamic dynamic = servletContext
                        .addFilter("@htmlx:" + oftenPath, new HtmlxFilter(oftenServlet, oftenPath));
                dynamic.addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, path);
//        dynamic.setAsyncSupported(true);
            }


        }
    }

    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Throwable
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        HttpServletResponse response = oftenObject.getRequest().getOriginalResponse();

        HtmlxDoc lastDoc;

        if (request.getAttribute(HtmlxDoc.ResponseType.class.getName()) == HtmlxDoc.ResponseType.Next)
        {
            lastDoc = (HtmlxDoc) request.getAttribute(HtmlxDoc.class.getName() + "@lastDoc");
            if (lastDoc == null)
            {
                throw new RuntimeException("expected last htmlxDoc");
            }

            if (!HttpCacheUtil.isCacheIneffectiveWithModified(lastDoc.getLastModified(), request, response))
            {
                if (cacheSeconds > 0)
                {
                    HttpCacheUtil.setCacheWithModified(cacheSeconds, lastDoc.getLastModified(), response);
                }
            } else
            {
                invokeDoc(oftenObject, request, response, lastDoc, porterOfFun);
            }
        } else
        {
            String rpath = request.getRequestURI().substring(request.getContextPath().length());
            if (rpath.endsWith("/"))
            {
                rpath += this.index;
            }
            String name = OftenStrUtil.getNameFormPath(rpath);
            if (Arrays.binarySearch(this.htmlSuffix, OftenStrUtil.getSuffix(name)) < 0)
            {
                //非html文件
                request.setAttribute(HtmlxDoc.ResponseType.class.getName(), HtmlxDoc.ResponseType.ServletDefault);
                return null;
            }

            ServletContext servletContext = request.getServletContext();
            String filePath = servletContext.getRealPath(rpath);

            File file = new File(filePath);
            long lastModified;
            if (!file.exists())
            {
                if (isDebug)
                {
                    if (OftenTool.notEmpty(otherwisePagePath))
                    {
                        String path = servletContext.getRealPath(otherwisePagePath);
                        if (path != null)
                        {
                            File ofile = new File(path);
                            if (ofile.exists())
                            {
                                otherwiseLastmodified = ofile.lastModified();
                            }
                        }
                    }
                }
                lastModified = otherwiseLastmodified;
            } else
            {
                lastModified = file.lastModified();
            }


            if (!HttpCacheUtil.isCacheIneffectiveWithModified(lastModified, request, response))
            {
                if (cacheSeconds > 0)
                {
                    HttpCacheUtil.setCacheWithModified(cacheSeconds, lastModified, response);
                }
            } else
            {
                Document document;
                HtmlxDoc.PageType pageType;
                if (file.exists())
                {
                    document = Jsoup.parse(file, encoding);
                    pageType = HtmlxDoc.PageType.Normal;
                } else
                {
                    if (otherwisePage == null || isDebug)
                    {
                        if (OftenTool.notEmpty(otherwisePagePath))
                        {
                            synchronized (this)
                            {
                                if (OftenTool.notEmpty(otherwisePagePath))
                                {
                                    String path = servletContext.getRealPath(otherwisePagePath);
                                    if (!this.isDebug)
                                    {
                                        otherwisePagePath = null;
                                    }
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
                        pageType = HtmlxDoc.PageType.OtherwiseHtml;
                    } else
                    {
                        InputStream in = new ByteArrayInputStream(otherwisePage);
                        document = Jsoup.parse(in, otherwisePageEncoding, "");
                        pageType = HtmlxDoc.PageType.OtherwisePage;
                    }
                    lastModified = otherwiseLastmodified;
                }
                HtmlxDoc htmlxDoc = new HtmlxDoc(this, rpath, document, pageType, lastModified);
                invokeDoc(oftenObject, request, response, htmlxDoc, porterOfFun);
            }

        }


        return null;
    }

    private void invokeDoc(OftenObject oftenObject, HttpServletRequest request, HttpServletResponse response,
            HtmlxDoc htmlxDoc, PorterOfFun porterOfFun) throws Throwable
    {
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
        HtmlxDoc.ResponseType responseType = htmlxDoc.getResponseType();
        request.setAttribute(HtmlxDoc.ResponseType.class.getName(), responseType);
        if (responseType == HtmlxDoc.ResponseType.Break)
        {
            //由开发者进行输出处理。
        } else if (responseType == HtmlxDoc.ResponseType.ServletDefault)
        {
            //执行默认的。
        } else if (responseType == HtmlxDoc.ResponseType.Normal)
        {
            response.setContentType(htmlxDoc.getContentType());
            response.setCharacterEncoding(htmlxDoc.getEncoding());
            if (htmlxDoc.willCache() && htmlxDoc.getCacheSeconds() > 0)
            {
                HttpCacheUtil.setCacheWithModified(htmlxDoc.getCacheSeconds(), htmlxDoc.getLastModified(), response);
            }
            Document document = (Document) htmlxDoc.getDocument();
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
                htmlStr = OftenStrUtil.replaceSharpProperties(htmlStr, map, "");
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

    @Override
    public boolean needInvoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn)
    {
        return true;
    }

    @Override
    public int compareTo(HtmlxHandle htmlxHandle)
    {
        if (this.order > htmlxHandle.order)
        {
            return 1;
        } else if (this.order < htmlxHandle.order)
        {
            return -1;
        } else
        {
            return 0;
        }
    }
}
