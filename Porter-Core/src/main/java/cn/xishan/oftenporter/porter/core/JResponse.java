package cn.xishan.oftenporter.porter.core;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应结果的封装.
 * <pre>
 * 1.结果为一个json对象，含有的属性为下面的某几个:
 *   {@linkplain #CODE_FIELD},{@linkplain #RESULT_FIELD},{@linkplain #DESCRIPTION_FIELD}
 * 2.结果码(code)为0表示成功，其他表示不成功（不同值对应不同意思）
 * 3.desc属性表示描述；rs表示返回的结果数据；uri表示请求的uri，当发生异常，会自动设置该值。
 *
 * </pre>
 *
 * @author Administrator
 */
public class JResponse
{

    public static class JResponseFormatException extends Exception
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public JResponseFormatException()
        {

        }

        public JResponseFormatException(String info)
        {
            super(info);
        }

        public JResponseFormatException(Throwable throwable)
        {
            super(throwable);
        }

    }

    public static final String CODE_FIELD = "code", CODE_NAME_FIELD = "cname";
    public static final String RESULT_FIELD = "rs";
    public static final String DESCRIPTION_FIELD = "desc";
    //public static final String REQUEST_URI_FIELD = "uri";


    @Deprecated
    public static final JResponse SUCCESS_RESPONSE = new JResponse(ResultCode.SUCCESS);

    private ResultCode code = ResultCode.Other;
    private String description;
    private Object result;
    private Throwable exCause;

    private static final Logger LOGGER = LoggerFactory.getLogger(JResponse.class);

    public JResponse(ResultCode code)
    {
        setCode(code);
    }

    public JResponse(int code)
    {
        setCode(code);
    }

    public JResponse()
    {

    }

    private static String getString(JSONObject jsonObject, String name)
    {
        if (jsonObject.containsKey(name))
        {
            return jsonObject.getString(name);
        } else
        {
            return null;
        }
    }

    /**
     * 设置异常原因
     *
     * @param exCause
     */
    public void setExCause(Throwable exCause)
    {
        Throwable cause = exCause.getCause();
        if (cause == null)
        {
            cause = exCause;
        }
        this.exCause = cause;
    }

    /**
     * 得到异常原因
     */
    public Throwable getExCause()
    {
        return exCause;
    }


    private static Object getResult(JSONObject jsonObject)
    {
        Object result = null;
        if (jsonObject.containsKey(RESULT_FIELD))
        {
            result = jsonObject.get(RESULT_FIELD);
            if (result instanceof String && ("true".equals(result) || "false".equals(result)))
            {
                result = Boolean.parseBoolean(result.toString());
            }
        }
        return result;
    }

    /**
     * 从对应的json字符转换成JResponse
     *
     * @param json
     * @return
     * @throws JResponseFormatException
     */
    public static JResponse fromJSON(String json) throws JResponseFormatException
    {
        try
        {
            JSONObject jsonObject = JSON.parseObject(json);
            int code = jsonObject.getIntValue(CODE_FIELD);
            String desc = getString(jsonObject, DESCRIPTION_FIELD);

            Object result = getResult(jsonObject);

            JResponse jsonResponse = new JResponse();
            jsonResponse.setCode(ResultCode.toResponseCode(code));
            jsonResponse.setDescription(desc);
            jsonResponse.setResult(result);
            return jsonResponse;
        } catch (Exception e)
        {
            throw new JResponseFormatException(e);
        }
    }

    /**
     * 设置结果码,默认为{@linkplain ResultCode#OK_BUT_FAILED OK_BUT_FAILED}.
     *
     * @param code
     */
    public void setCode(ResultCode code)
    {
        this.code = code;
    }

    public void setCode(int code)
    {
        this.code = ResultCode.toResponseCode(code);
    }

    /**
     * 设置描述信息
     *
     * @param description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * 设置结果对象
     *
     * @param result
     */
    public void setResult(Object result)
    {
        this.result = result;
    }

    public ResultCode getCode()
    {
        return code;
    }

    /**
     * 如果异常不为空则，抛出异常。
     *
     * @param <T>
     * @return
     */
    public <T> T getResult()
    {
        _throwExCause();
        return (T) result;
    }

    public boolean isSuccess()
    {
        return code == ResultCode.SUCCESS || code == ResultCode.OK;
    }

    public boolean isNotSuccess()
    {
        return code != ResultCode.SUCCESS && code != ResultCode.OK;
    }

    /**
     * 如果异常信息不为空，则抛出。
     */
    public void throwExCause()
    {
        _throwExCause();
    }


    private final void _throwExCause()
    {
        if (exCause != null)
        {
            try
            {
                throw exCause;
            } catch (Throwable throwable)
            {
                LOGGER.debug(throwable.getMessage(), throwable);
            }
        }
    }

    /**
     * 转换为字符串
     */
    @Override
    public String toString()
    {
        JSONObject json = new JSONObject(3);
        ResultCode resultCode = code != null ? code : ResultCode.Other;
        json.put(CODE_FIELD, resultCode.toCode());
        json.put(CODE_NAME_FIELD, resultCode.name());
        json.put(DESCRIPTION_FIELD, description);
        if (result != null)
        {
            if (result instanceof JSONObject || result instanceof JSONArray)
            {
                json.put(RESULT_FIELD, result);
            } else
            {
                json.put(RESULT_FIELD, String.valueOf(result));
            }
        }


        return json.toString();
    }

    public static JResponse success(Object result)
    {
        JResponse jResponse = new JResponse(ResultCode.SUCCESS);
        jResponse.setResult(result);
        return jResponse;
    }

    public static JResponse failed(String desc)
    {
        JResponse jResponse = new JResponse(ResultCode.OK_BUT_FAILED);
        jResponse.setDescription(desc);
        return jResponse;
    }

}
