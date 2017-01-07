package cn.xishan.oftenporter.porter.core.base;

import java.util.Map;

/**
 * 对应于请求。
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public interface WRequest
{
    /**
     * 得到参数，例如地址参数和表单参数等。
     *
     * @param name 参数名称
     */
    Object getParameter(String name);

//    /**
//     * 得到所有参数的名称。
//     */
//    Enumeration<String> getParameterNames();

    /**
     * 得到所有参数的map。
     * @return
     */
    Map<String,Object> getParameterMap();

    String getPath();

    PortMethod getMethod();

    /**
     * 获得最原始的响应对象，例如在servlet中可能需要HttpServletResponse.
     * @return
     */
    public Object getOriginalResponse();

    /**
     * 获得最原始的请求对象，例如在servlet中可能需要HttpServletRequest.
     * @return
     */
    public Object getOriginalRequest();

}
