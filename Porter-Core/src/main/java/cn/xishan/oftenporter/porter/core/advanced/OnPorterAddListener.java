package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/18.
 */
public interface OnPorterAddListener
{
    /**
     * 接口已经被扫描了。
     * @param contextName
     * @param porter
     * @return 返回true表示不添加，false表示添加。
     */
    boolean onAdding(String contextName,Porter porter);

    /**
     * 是否需要扫描该类。
     * @param contextName
     * @param clazz
     * @return 返回true表示不扫描，false表示扫描。
     */
    boolean onSeeking(String contextName,Class<?> clazz);
}
