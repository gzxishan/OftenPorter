package cn.xishan.oftenporter.porter.core.advanced;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/18.
 */
public interface IListenerAdder<T>
{
    void addListener(String name,T listener);

    /**
     * 返回任务名称
     * @param listener
     * @return
     */
    String addListener(T listener);
    T removeListener(String name);

    /**
     *
     * @param order 0表示无顺序要求,1表示添加时的顺序，-1表示与添加时的顺序相反
     * @return
     */
    Enumeration<T> listeners(int order);
}
