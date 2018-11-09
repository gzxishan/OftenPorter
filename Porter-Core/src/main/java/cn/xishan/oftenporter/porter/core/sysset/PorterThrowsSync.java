package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;

/**
 * <pre>
 * 如果返回结果为{@linkplain JResponse}且code不为成功、或者返回{@linkplain Throwable},则会抛出{@linkplain OftenCallException}异常，另见{@linkplain PorterSync}
 * </pre>
 * Created by chenyg on 2018-03-02.
 */
public interface PorterThrowsSync extends PorterSync
{
}
