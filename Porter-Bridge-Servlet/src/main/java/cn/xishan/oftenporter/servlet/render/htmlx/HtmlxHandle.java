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
import cn.xishan.oftenporter.servlet.StartupServlet;
import org.jsoup.Jsoup;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
@Htmlx(path = "")
public class HtmlxHandle extends AspectOperationOfPortIn.HandleAdapter<Htmlx> implements Comparable<HtmlxHandle>
{
    private static final String HANDLE_KEY = HtmlxHandle.class.getName() + "@-handle-key-";
//    private static final String HANDLE_ATTR = HtmlxHandle.class.getName() + "@-last-handle-";

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
    private HtmlxDoc.ResponseType defaultResponseType;

    private long otherwiseLastmodified = System.currentTimeMillis();


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

            Set<String> stringSet = new HashSet<>();
            OftenTool.addAll(stringSet, current.path());
            this.path = stringSet.toArray(new String[0]);

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
            this.defaultResponseType = HtmlxDoc.ResponseType.valueOf(
                    getValue(current.defaultResponseType().name(), classHtmlx.defaultResponseType().name(),
                            defaultHtmlx.defaultResponseType().name()));

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
            StartupServlet startupServlet = oftenObject.getContextSet(StartupServlet.class);
            HtmlxHandle[] handles = handleList.toArray(new HtmlxHandle[0]);
            Arrays.sort(handles);

            Set<String> endPaths = new HashSet<>();
            for (HtmlxHandle htmlxHandle : handles)
            {
                String oftenPath = htmlxHandle.oftenPath;
                String[] path = htmlxHandle.path;
                OftenTool.addAll(endPaths, path);
                FilterRegistration.Dynamic dynamic = servletContext
                        .addFilter("@htmlx:" + oftenPath, new HtmlxFilter(startupServlet, oftenPath));
                dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),
                        false, path);
                dynamic.setAsyncSupported(true);
            }
            FilterRegistration.Dynamic dynamic = servletContext
                    .addFilter("@htmlx-end:" + oftenPath, new HtmlxEndFilter());
            dynamic.setAsyncSupported(true);
            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),
                    true, endPaths.toArray(new String[0]));
            //dynamic.setAsyncSupported(true);
        }
    }

    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Throwable
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        HttpServletResponse response = oftenObject.getRequest().getOriginalResponse();

        RenderData lastRenderData = (RenderData) request.getAttribute(RenderData.class.getName());

        if (lastRenderData != null)
        {
            HtmlxDoc htmlxDoc = lastRenderData.getHtmlxDoc();
            if (cacheSeconds > 0 && !HttpCacheUtil
                    .isCacheIneffectiveWithModified(htmlxDoc.getLastModified(), request, response))
            {
                request.setAttribute(HtmlxDoc.ResponseType.class.getName(), HtmlxDoc.ResponseType.Break);
                HttpCacheUtil.setCacheWithModified(cacheSeconds, htmlxDoc.getLastModified(), response);
            } else
            {
                invokeDoc(oftenObject, request, response, lastRenderData, porterOfFun);
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


            if (cacheSeconds > 0 && !HttpCacheUtil.isCacheIneffectiveWithModified(lastModified, request, response))
            {
                request.setAttribute(HtmlxDoc.ResponseType.class.getName(), HtmlxDoc.ResponseType.Break);
                HttpCacheUtil.setCacheWithModified(cacheSeconds, lastModified, response);
            } else
            {
                HtmlxDoc.PageType pageType;

                IDocGetter docGetter;

                if (file.exists())
                {
                    docGetter = () -> Jsoup.parse(file, encoding);
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
                        docGetter = () -> {
                            InputStream in = new ByteArrayInputStream(otherwiseHtml);
                            return Jsoup.parse(in, encoding, "");
                        };
                        pageType = HtmlxDoc.PageType.OtherwiseHtml;
                    } else
                    {
                        docGetter = () -> {
                            InputStream in = new ByteArrayInputStream(otherwisePage);
                            return Jsoup.parse(in, otherwisePageEncoding, "");
                        };

                        pageType = HtmlxDoc.PageType.OtherwisePage;
                    }
                    lastModified = otherwiseLastmodified;
                }
                HtmlxDoc htmlxDoc = new HtmlxDoc(this, rpath, pageType, defaultResponseType, lastModified, docGetter);
                RenderData renderData = new RenderData(htmlxDoc);
                request.setAttribute(RenderData.class.getName(), renderData);
                invokeDoc(oftenObject, request, response, renderData, porterOfFun);
            }

        }


        return null;
    }

    private void invokeDoc(OftenObject oftenObject, HttpServletRequest request, HttpServletResponse response,
            RenderData renderData, PorterOfFun porterOfFun) throws Throwable
    {

        HtmlxDoc htmlxDoc = renderData.getHtmlxDoc();
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

        Object rt = porterOfFun.invokeByHandleArgs(oftenObject, htmlxDoc);
        renderData.addReturnObject(rt);

        HtmlxDoc.ResponseType responseType = htmlxDoc.getResponseType();
        request.setAttribute(HtmlxDoc.ResponseType.class.getName(), responseType);
        if (responseType == HtmlxDoc.ResponseType.Break)
        {
            //由开发者进行输出处理。
        } else if (responseType == HtmlxDoc.ResponseType.ServletDefault)
        {
            //执行默认的。
        } else if (responseType == HtmlxDoc.ResponseType.Next)
        {

        } else if (responseType == HtmlxDoc.ResponseType.Normal)
        {
            response.setContentType(htmlxDoc.getContentType());
            response.setCharacterEncoding(htmlxDoc.getEncoding());
            if (htmlxDoc.willCache() && htmlxDoc.getCacheSeconds() > 0)
            {
                HttpCacheUtil.setCacheWithModified(htmlxDoc.getCacheSeconds(), htmlxDoc.getLastModified(), response);
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
