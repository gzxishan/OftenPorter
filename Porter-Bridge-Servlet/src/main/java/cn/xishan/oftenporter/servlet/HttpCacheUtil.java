package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.util.OftenTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于控制缓存。
 * Created by chenyg on 2017/2/10.
 */
public class HttpCacheUtil
{
    public interface Callback
    {
        void call(HttpServletRequest request, HttpServletResponse response);
    }

    /**
     * 最终缓存是否有效的判断：
     * <pre>
     *          boolean isChanged = false;
     *             if (isChangeByDtime != null)
     *             {
     *                 isChanged = isChangeByDtime;
     *             }
     *
     *             if (!isChanged)
     *             {
     *                 if (setChange != null)
     *                 {
     *                     isChanged = setChange || checkChange;
     *                 } else
     *                 {
     *                     isChanged = checkChange;
     *                 }
     *             }
     * </pre>
     */
    public interface ICtrl
    {

        /**
         * 改变时的回调
         *
         * @param callback
         * @return
         */
        ICtrl changed(Callback callback);

        /**
         * 设置缓存信息
         *
         * @param forceSeconds
         * @param etag
         * @return
         */
        ICtrl cacheInfo(int forceSeconds, String etag);

        /**
         * 未改变时、返回304状态。
         *
         * @return
         */
        ICtrl unchanged304();

        /**
         * @param forceSeconds 单位秒
         * @param lastModified 单位毫秒
         * @return
         */
        ICtrl cacheInfo(int forceSeconds, long lastModified);

        /**
         * 未改变时的回调
         *
         * @param callback
         * @return
         */
        ICtrl unchanged(Callback callback);

        /**
         * 手动设置资源是否改变。
         *
         * @param isChange
         * @return
         */
        ICtrl setIsChange(boolean isChange);

        /**
         * 如果上次访问的时间到此时大于dseconds、则判断缓存失效，另见{@linkplain #setIsChange(boolean)}。
         *
         * @param dseconds
         * @return
         */
        ICtrl setIsChangeByDtime(int dseconds);

        /**
         * 完成设置
         */
        void done();
    }

    private static class ICtrlImpl implements ICtrl
    {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private Callback changedCallback;
        private Callback unchangedCallback;
        private Integer forceSeconds;
        private String etag;
        private Long lastModified;
        private Boolean response304;
        private Boolean isChangeByDtime;
        private Boolean setChange;
        private boolean checkChange;

        public ICtrlImpl(HttpServletRequest request, HttpServletResponse response, boolean isChanged)
        {
            this.request = request;
            this.response = response;
            this.checkChange = isChanged;
        }

        @Override
        public ICtrl changed(Callback callback)
        {
            this.changedCallback = callback;
            return this;
        }

        @Override
        public ICtrl cacheInfo(int forceSeconds, String etag)
        {
            this.forceSeconds = forceSeconds;
            this.etag = etag;
            return this;
        }

        @Override
        public ICtrl unchanged304()
        {
            response304 = true;
            return this;
        }

        @Override
        public ICtrl cacheInfo(int forceSeconds, long lastModified)
        {
            this.forceSeconds = forceSeconds;
            this.lastModified = lastModified;
            return this;
        }

        @Override
        public ICtrl unchanged(Callback callback)
        {
            this.unchangedCallback = changedCallback;
            return this;
        }

        @Override
        public ICtrl setIsChange(boolean isChange)
        {
            this.setChange = isChange;
            return this;
        }

        @Override
        public ICtrl setIsChangeByDtime(int dseconds)
        {
            long since = request.getDateHeader("If-Modified-Since") / 1000;
            this.isChangeByDtime = (System.currentTimeMillis() / 1000 - since) > dseconds;
            return this;
        }

        @Override
        public void done()
        {
            boolean isChanged = false;
            if (isChangeByDtime != null)
            {
                isChanged = isChangeByDtime;
            }

            if (!isChanged)
            {
                if (setChange != null)
                {
                    isChanged = setChange || checkChange;
                } else
                {
                    isChanged = checkChange;
                }
            }

            if (isChanged)
            {
                if (changedCallback != null)
                {
                    changedCallback.call(request, response);
                }
            } else
            {
                if (forceSeconds != null)
                {
                    if (OftenTool.notEmpty(etag))
                    {
                        setCacheWithEtag(forceSeconds, etag, response);
                    }

                    if (OftenTool.notEmpty(lastModified))
                    {
                        setCacheWithModified(forceSeconds, lastModified, response);
                    }
                }

                if (unchangedCallback != null)
                {
                    unchangedCallback.call(request, response);
                }
                if (response304 != null)
                {
                    notModified(response);
                }
            }
        }
    }


    public static ICtrl checkWithEtag(String etag, HttpServletRequest request,
            HttpServletResponse response)
    {
        ICtrl ctrl = new ICtrlImpl(request, response, isCacheIneffectiveWithEtag(etag, request, response));
        return ctrl;
    }

    public static ICtrl checkWithModified(long lastModified, HttpServletRequest request,
            HttpServletResponse response)
    {
        ICtrl ctrl = new ICtrlImpl(request, response, isCacheIneffectiveWithModified(lastModified, request, response));
        return ctrl;
    }

    /**
     * 检测资源是否过期，若过期了则重新设置缓存时间.
     *
     * @param sec                   缓存多少秒
     * @param modelLastModifiedDate 上次修改日期
     * @param request
     * @param response
     * @return true：发送数据；false：不发送.
     */
    @Deprecated
    public static boolean checkHeaderCache(long sec,
            long modelLastModifiedDate, HttpServletRequest request,
            HttpServletResponse response)
    {

        // convert seconds to ms.
        long adddaysM = sec * 1000;
        long header = request.getDateHeader("If-Modified-Since");
        long now = System.currentTimeMillis();
        if (header > 0 && adddaysM > 0)
        {
            if (modelLastModifiedDate > header)
            {
                // adddays = 0; // reset
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


    public static void notModified(HttpServletResponse response)
    {
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
    @Deprecated
    public static boolean setRespHeaderCache(long sec,
            HttpServletRequest request, HttpServletResponse response)
    {
        long adddaysM = sec * 1000;
        String maxAgeDirective = "max-age=" + sec;
        response.setHeader("Cache-Control", maxAgeDirective);
        response.addDateHeader("Last-Modified", System.currentTimeMillis());
        response.addDateHeader("Expires", System.currentTimeMillis() + adddaysM);
        return true;
    }


    /**
     * 设置缓存。
     *
     * @param forceSeconds 强制缓存时间，在这段时间内浏览器只加载缓存（如果存在资源）、而不会访问服务器进行比对。
     * @param lastModified 资源上次修改时间(单位毫秒)。
     * @param response
     */
    public static void setCacheWithModified(int forceSeconds, long lastModified, HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "max-age=" + forceSeconds);
        response.addDateHeader("Expires", System.currentTimeMillis() + forceSeconds * 1000);
        response.addDateHeader("Last-Modified", lastModified);
    }

    /**
     * 设置缓存。
     *
     * @param forceSeconds 强制缓存时间，在这段时间内浏览器只加载缓存（如果存在资源）、而不会访问服务器进行比对。
     * @param etag         资源唯一标识（优先级高于Last-Modified）
     * @param response
     */
    public static void setCacheWithEtag(int forceSeconds, String etag, HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "max-age=" + forceSeconds);
        response.addDateHeader("Expires", System.currentTimeMillis() + forceSeconds * 1000);
        response.setHeader("ETag", etag);
    }

    /**
     * 判断客户端缓存是否失效,如果没有失效会设置状态码为304。
     *
     * @param lastModified 资源上次修改时间(单位毫秒)。
     * @param request
     * @return
     */
    public static boolean isCacheIneffectiveWithModified(long lastModified, HttpServletRequest request,
            HttpServletResponse response)
    {
        long since = request.getDateHeader("If-Modified-Since") / 1000;
        if (lastModified > 0)
        {
            lastModified /= 1000;
        }
        if (lastModified == since)
        {
            notModified(response);
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * 判断客户端缓存是否失效,如果没有失效会设置状态码为304。
     *
     * @param etag    资源唯一标识（优先级高于If-Modified-Since）
     * @param request
     * @return
     */
    public static boolean isCacheIneffectiveWithEtag(String etag, HttpServletRequest request,
            HttpServletResponse response)
    {
        String previousTag = request.getHeader("If-None-Match");
        boolean b = !etag.equals(previousTag);
        if (!b)
        {
            notModified(response);
        }
        return b;
    }
}
