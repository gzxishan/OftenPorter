package cn.xishan.oftenporter.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于控制缓存。
 * Created by chenyg on 2017/2/10.
 */
public class HttpCacheUtil
{
    /**
     * 检测资源是否过期，若过期了则重新设置缓存时间.
     *
     * @param sec                   缓存多少秒
     * @param modelLastModifiedDate 上次修改日期
     * @param request
     * @param response
     * @return true：发送数据；false：不发送.
     */
    public static boolean checkHeaderCache(long sec,
            long modelLastModifiedDate, HttpServletRequest request,
            HttpServletResponse response)
    {
        request.setAttribute("myExpire", sec);

        // convert seconds to ms.
        long adddaysM = sec * 1000;
        long header = request.getDateHeader("If-Modified-Since");
        long now = System.currentTimeMillis();
        if (header > 0 && adddaysM > 0)
        {
            if (modelLastModifiedDate > header)
            {
                // adddays = 0; // reset
                response.setStatus(HttpServletResponse.SC_OK);
                return true;
            }
            if (header + adddaysM > now)
            {
                notModified(response);
                return false;
            }
        }

        // if over expire data, see the Etags;
        // ETags if ETags no any modified
        String previousToken = request.getHeader("If-None-Match");
        if (previousToken != null
                && previousToken.equals(Long.toString(modelLastModifiedDate)))
        {
            notModified(response);
            return false;
        }
        // if th model has modified , setup the new modified date
        response.setHeader("ETag", Long.toString(modelLastModifiedDate));
        setRespHeaderCache(sec, request, response);
        return true;

    }



    public static void notModified(HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }


    /**
     * 设置资源缓存时间
     *
     * @param sec      缓存多少秒
     * @param request
     * @param response
     * @return
     */
    public static boolean setRespHeaderCache(long sec,
            HttpServletRequest request, HttpServletResponse response)
    {
        request.setAttribute("myExpire", sec);

        long adddaysM = sec * 1000;
        String maxAgeDirective = "max-age=" + sec;
        response.setHeader("Cache-Control", maxAgeDirective);
        response.setStatus(HttpServletResponse.SC_OK);
        response.addDateHeader("Last-Modified", System.currentTimeMillis());
        response.addDateHeader("Expires", System.currentTimeMillis() + adddaysM);
        return true;
    }
}
