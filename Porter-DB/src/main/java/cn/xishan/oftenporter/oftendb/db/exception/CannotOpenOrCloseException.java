package cn.xishan.oftenporter.oftendb.db.exception;

import cn.xishan.oftenporter.oftendb.db.DBException;

/**
 * Created by chenyg on 2017-04-05.
 */
public class CannotOpenOrCloseException extends DBException
{
    public CannotOpenOrCloseException()
    {
    }

    public CannotOpenOrCloseException(String message)
    {
        super(message);
    }

    public CannotOpenOrCloseException(Throwable throwable)
    {
        super(throwable);
    }
}
