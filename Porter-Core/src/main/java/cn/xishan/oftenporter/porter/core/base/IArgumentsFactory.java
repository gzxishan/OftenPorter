package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.NeceParam;
import cn.xishan.oftenporter.porter.core.annotation.UneceParam;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;

import java.lang.reflect.Method;

/**
 * <p>
 * 支持的注解：{@linkplain NeceParam},{@linkplain UneceParam}
 * </p>
 * <p>
 * 支持的默认参数：WObject
 * </p>
 * <p>
 * servlet下支持的默认参数：HttpServletRequest,HttpServletResponse,ServletContext,HttpSession
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public interface IArgumentsFactory
{
    interface IArgsHandle
    {
        Object[] getInvokeArgs(WObject wObject, Method method, Object[] args);
    }

    /**
     * 启动时会调用一次。
     *
     * @param porterOfFun
     * @return
     */
    void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception;

    /**
     * 启动时会调用一次。
     *
     * @param porterOfFun
     * @return
     */
    IArgsHandle getArgsHandle(PorterOfFun porterOfFun) throws Exception;
}
