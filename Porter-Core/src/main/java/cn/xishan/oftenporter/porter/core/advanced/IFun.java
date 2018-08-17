package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-08-17.
 */
public interface IFun
{
    String[] tieds(Porter porter,Method method, _PortIn methodIn);
}
