package cn.xishan.oftenporter.porter.core.exception;

/**
 * @author Created by https://github.com/CLovinr on 2019-05-14.
 */
public class AutoSetException extends RuntimeException
{
    public AutoSetException(String message)
    {
        super(message);
    }

    public AutoSetException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AutoSetException(Throwable cause)
    {
        super(cause);
    }
}
