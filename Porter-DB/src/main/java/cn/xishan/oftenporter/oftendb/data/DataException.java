package cn.xishan.oftenporter.oftendb.data;

public class DataException extends Exception
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DataException(Throwable throwable)
    {
        super(throwable);
    }

    public DataException(String info)
    {
        super(info);
    }

    public DataException()
    {
    }

}
