package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.reflect.Method;

/**
 * <p>
 * 支持的注解：{@linkplain Nece},{@linkplain Unece}
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
        Object[] getInvokeArgs(WObject wObject, @MayNull PorterOfFun fun, Method method, Object[] args);

        boolean hasParameterType(WObject wObject, @MayNull PorterOfFun fun, Method method, Class<?> type);
    }

    /**
     * 启动时会调用一次。
     *
     * @param porterOfFun
     * @return
     */
    void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception;


    IArgsHandle getArgsHandle(PorterOfFun porterOfFun);
}
