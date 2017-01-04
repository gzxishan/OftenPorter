package cn.xishan.oftenporter.porter.core.exception;

/**
 * 初始化异常。
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
public class InitException extends RuntimeException
{
    public InitException(Throwable cause)
    {
        super(cause);
    }

    public InitException(String message)
    {
        super(message);
    }
}
