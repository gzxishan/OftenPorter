package cn.xishan.oftenporter.porter.core.exception;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by cyg on 2015/10/23.
 *
 * @see #theJResponse()
 */
public class OftenCallException extends RuntimeException
{
    private JResponse jResponse;
    private JSONObject jsonObject;

    /**
     * 内部构建一个JResponse
     *
     * @param msg
     */
    public OftenCallException(String msg)
    {
        this(ResultCode.OK_BUT_FAILED, msg);
    }

    /**
     * 内部构建一个JResponse
     *
     * @param msg
     */
    public OftenCallException(int code, String msg)
    {
        JResponse jResponse = new JResponse(code);
        jResponse.setDescription(msg);
        this.jResponse = jResponse;
    }

    /**
     * 内部构建一个JResponse
     *
     * @param msg
     */
    public OftenCallException(ResultCode code, String msg)
    {
        JResponse jResponse = new JResponse(code);
        jResponse.setDescription(msg);
        this.jResponse = jResponse;
    }

    public OftenCallException(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public OftenCallException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }

    public OftenCallException(Throwable cause)
    {
        super(cause);
    }

    public OftenCallException(JResponse jResponse)
    {
        setJResponse(jResponse);
    }


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

    public JSONObject toJSON()
    {
        if (jResponse != null)
        {
            return jResponse.toJSON();
        } else if (jsonObject != null)
        {
            return jsonObject;
        } else
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(OftenTool.getMessage(this));
            return jResponse.toJSON();
        }
    }

    @Override
    public String toString()
    {
        JSONObject json = toJSON();
        return json == null ? super.toString() : json.toJSONString();
    }
}
