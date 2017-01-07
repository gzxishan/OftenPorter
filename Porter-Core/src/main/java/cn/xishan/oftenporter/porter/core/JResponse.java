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
 *   {@linkplain #CODE_FILED},{@linkplain #RESULT_FILED},{@linkplain #DESCRIPTION_FILED}
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

    public static final String CODE_FILED = "code";
    public static final String RESULT_FILED = "rs";
    public static final String DESCRIPTION_FILED = "desc";
    //public static final String REQUEST_URI_FILED = "uri";


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
        if (jsonObject.containsKey(RESULT_FILED))
        {
            result = jsonObject.get(RESULT_FILED);
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
            int code = jsonObject.getIntValue(CODE_FILED);
            String desc = getString(jsonObject, DESCRIPTION_FILED);

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

    public <T> T getResult()
    {
        return (T) result;
    }

    public boolean isSuccess()
    {
        return code == ResultCode.SUCCESS;
    }

    /**
     * 如果异常信息不为空，则抛出。
     */
    public void throwExCause()
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
        json.put(CODE_FILED, code != null ? code.toCode()
                : ResultCode.Other.toCode());
        json.put(DESCRIPTION_FILED, description);
        if (result != null)
        {
            if (result instanceof JSONObject || result instanceof JSONArray)
            {
                json.put(RESULT_FILED, result);
            } else
            {
                json.put(RESULT_FILED, String.valueOf(result));
            }
        }


        return json.toString();
    }

}
