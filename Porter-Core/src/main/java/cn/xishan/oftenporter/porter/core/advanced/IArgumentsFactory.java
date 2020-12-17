package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.FunParam;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;

import java.lang.reflect.Method;

/**
 * <p>
 * porter函数的形参处理。
 * </p>
 * <p>
 * 支持的注解：{@linkplain Nece},{@linkplain Unece}
 * </p>
 * <p>
 * 支持的默认参数：{@linkplain OftenObject},{@linkplain PorterOfFun}
 * </p>
 * <p>
 * servlet下支持的默认参数：HttpServletRequest,HttpServletResponse,ServletContext,HttpSession
 * </p>
 * <p>
 * 另见：{@linkplain DefaultArgumentsFactory}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public interface IArgumentsFactory
{
    interface IArgsHandle
    {
        /**
         * @param oftenObject
         * @param fun
         * @param method
         * @param args        支持{@linkplain FunParam}
         * @return
         */
        Object[] getInvokeArgs(OftenObject oftenObject, @MayNull PorterOfFun fun, Method method, Object[] args);

        /**
         * 获取实际参数在函数形参列表中的索引。
         *
         * @return
         */
        int[] getArgsIndex();

        boolean hasParameterType(OftenObject oftenObject, @MayNull PorterOfFun fun, Method method, Class<?> type);
    }

    /**
     * 启动时会调用一次,用于设置{@linkplain PorterOfFun#setArgsHandle(IArgsHandle)}。
     *
     * @param porterOfFun
     * @return
     */
    void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception;

}
