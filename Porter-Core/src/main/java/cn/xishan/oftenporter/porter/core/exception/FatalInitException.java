package cn.xishan.oftenporter.porter.core.exception;

/**
 * 严重的初始化异常。
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class FatalInitException extends Exception
{
    public FatalInitException(Throwable cause)
    {
        super(cause);
    }

    public FatalInitException(String message)
    {
        super(message);
    }
}
