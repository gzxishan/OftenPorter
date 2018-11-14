package cn.xishan.oftenporter.servlet.render.jsp;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/27.
 */
public class JspOption
{
    static final JspOption DEFAULT = new JspOption();

    private String prefix = "/WEB-INF/jsp/";

    private String suffix = ".jsp";


    private boolean useStdTag = true;

    private boolean enableEL = true;

    private String pageEncoding = "utf-8";


    private String appendJspContent = null;


    public String getPrefix()
    {
        return prefix;
    }

    /**
     * 设置前缀，默认为"/WEB-INF/jsp/"
     *
     * @param prefix
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public String getSuffix()
    {
        return suffix;
    }

    /**
     * 设置后缀，默认为".jsp"
     *
     * @param suffix
     */
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public boolean isUseStdTag()
    {
        return useStdTag;
    }

    /**
     * 是否自动使用标准标签库,当{@linkplain #suffix}为".jsp"且servletContext.getRealPath()不为null时有效。
     */
    public void setUseStdTag(boolean useStdTag)
    {
        this.useStdTag = useStdTag;
    }

    public boolean isEnableEL()
    {
        return enableEL;
    }

    /**
     * 是否开头el表达式，默认为true。
     *
     * @param enableEL
     */
    public void setEnableEL(boolean enableEL)
    {
        this.enableEL = enableEL;
    }

    public String getPageEncoding()
    {
        return pageEncoding;
    }

    /**
     * 设置页面编码，默认为utf-8
     *
     * @param pageEncoding
     */
    public void setPageEncoding(String pageEncoding)
    {
        this.pageEncoding = pageEncoding;
    }

    public String getAppendJspContent()
    {
        return appendJspContent;
    }

    /**
     * 添加的jsp内容
     */
    public void setAppendJspContent(String appendJspContent)
    {
        this.appendJspContent = appendJspContent;
    }
}
