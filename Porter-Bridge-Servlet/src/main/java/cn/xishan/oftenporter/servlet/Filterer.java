package cn.xishan.oftenporter.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现该接口的过滤器，支持框架的@AutoSet等注解,<strong>不要重载</strong>{@linkplain #init(FilterConfig)}。
 *
 * @author Created by https://github.com/CLovinr on 2019/5/9.
 */
public interface Filterer extends Filter
{

    /**
     * <strong>不要重载该方法.</strong>
     *
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    default void init(FilterConfig filterConfig) throws ServletException
    {
        synchronized (Filterer.class)
        {
            List<Filterer> filterers = (List<Filterer>) filterConfig.getServletContext()
                    .getAttribute(Filterer.class.getName());
            if (filterers == null)
            {
                filterers = new ArrayList<>();
                filterConfig.getServletContext().setAttribute(Filterer.class.getName(), filterers);
            }
            filterers.add(this);
        }
        doInit(filterConfig);
    }

    void doInit(FilterConfig filterConfig) throws ServletException;
}
