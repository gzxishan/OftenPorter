package cn.xishan.oftenporter.oftendb.db;

/**
 * 数据库操作异常
 *
 * @author Administrator
 */
public class DBException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DBException()
    {
    }

    public DBException(String message)
    {
        this(new RuntimeException(message));
    }

    public DBException(Throwable throwable)
    {
        super(throwable);
    }

}
