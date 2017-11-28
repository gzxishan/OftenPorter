package cn.xishan.oftenporter.servlet.render.jsp;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/27.
 */
public class JspOption
{
    static final JspOption DEFAULT = new JspOption();

    public String prefix = "/WEB-INF/jsp/";

    public String suffix = ".jsp";

    /**
     * 是否自动使用标准标签库,当{@linkplain #suffix}为".jsp"且servletContext.getRealPath()不为null时有效。
     */
    public boolean useStdTag = true;

    public boolean enableEL = true;

    public String pageEncoding = "utf-8";

    /**
     * 添加的jsp内容
     */
    public String appendJspContent = null;

}
