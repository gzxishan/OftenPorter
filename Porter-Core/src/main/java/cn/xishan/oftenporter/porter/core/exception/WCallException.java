package cn.xishan.oftenporter.porter.core.exception;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.util.WPTool;

/**
 * Created by 刚帅 on 2015/10/23.
 *
 * @see #theJResponse()
 */
public class WCallException extends RuntimeException
{
    public WCallException(String msg)
    {
        super(msg);
    }

    public WCallException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }

    public WCallException(Throwable cause)
    {
        super(cause);
    }

    public WCallException(JResponse jResponse)
    {
        setJResponse(jResponse);
    }

    private JResponse jResponse;

    public void setJResponse(JResponse jResponse)
    {
        this.jResponse = jResponse;
    }

    /**
     * 若返回值不为空则，且该异常被框架捕获，则向客户端返回该对象。
     *
     * @return
     */
    public JResponse theJResponse()
    {
        return jResponse;
    }

    @Override
    public String toString()
    {
        String str = getMessage();

        return WPTool.isEmpty(str) ? super.toString() : str;
    }
}
