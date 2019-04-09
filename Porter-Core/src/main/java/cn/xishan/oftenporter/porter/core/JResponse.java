package cn.xishan.oftenporter.porter.core;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

    /**
     * 自定义返回的对象。
     */
    public interface IObject
    {
        Object toCustomObject();
    }

    public static class JResponseFormatException extends RuntimeException
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
    /**
     * 见{@link IObject}
     */
    public static final String RESULT_FIELD = "rs", EXTRA_FIELD = "extra";
    public static final String DESCRIPTION_FIELD = "desc";
    //public static final String REQUEST_URI_FIELD = "uri";


    private Object code = ResultCode.Other;
    private String description;
    private Object result, extra;
    private Throwable exCause;

    public JResponse(ResultCode code)
    {
        setCode(code);
    }

    public JResponse(int code)
    {
        setCode(code);
    }

    public JResponse(ResultCode code, String description)
    {
        setCode(code);
        setDescription(description);
    }

    public JResponse(int code, String description)
    {
        setCode(code);
        setDescription(description);
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

    private static Object getExtra(JSONObject jsonObject)
    {
        Object result = null;
        if (jsonObject.containsKey(EXTRA_FIELD))
        {
            result = jsonObject.get(EXTRA_FIELD);
            if (result instanceof String && ("true".equals(result) || "false".equals(result)))
            {
                result = Boolean.parseBoolean(result.toString());
            }
        }
        return result;
    }

    public Object getExtra()
    {
        return extra;
    }

    public void setExtra(Object extra)
    {
        this.extra = extra;
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
        JSONObject jsonObject = JSON.parseObject(json);
        return fromJSONObject(jsonObject);
    }

    /**
     * 从对应的json转换成JResponse
     *
     * @param jsonObject
     * @return
     * @throws JResponseFormatException
     */
    public static JResponse fromJSONObject(JSONObject jsonObject) throws JResponseFormatException
    {
        try
        {
            int code = jsonObject.getIntValue(CODE_FIELD);
            String desc = getString(jsonObject, DESCRIPTION_FIELD);

            Object result = getResult(jsonObject);
            Object extra = getExtra(jsonObject);

            ResultCode resultCode = ResultCode.toResponseCode(code);

            JResponse jsonResponse = new JResponse();
            if (resultCode == null)
            {
                jsonResponse.setCode(code);
            } else
            {
                jsonResponse.setCode(resultCode);
            }
            jsonResponse.setDescription(desc);
            jsonResponse.setResult(result);
            jsonResponse.setExtra(extra);
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
        this.code = code;
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


    public int getIntCode()
    {
        if (code instanceof ResultCode)
        {
            return ((ResultCode) code).toCode();
        } else
        {
            return (int) code;
        }
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

    /**
     * 结果为json的情况。
     *
     * @return
     */
    public JSONObject resultJSON()
    {
        JSONObject json = getResult();
        return json;
    }

    public boolean resultBoolean()
    {
        boolean is = getResult();
        return is;
    }

    public int resultInt()
    {
        int n = getResult();
        return n;
    }

    public long resultLong()
    {
        long l = getResult();
        return l;
    }

    public String resultString()
    {
        String str = getResult();
        return str;
    }

    public JSONArray resultJSONArray()
    {
        JSONArray jsonArray = getResult();
        return jsonArray;
    }

    public boolean isSuccess()
    {
        return getIntCode() == ResultCode.SUCCESS.toCode();
    }

    public boolean isNotSuccess()
    {
        return getIntCode() != ResultCode.SUCCESS.toCode();
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
            RuntimeException runtimeException;
            if (exCause instanceof RuntimeException)
            {
                runtimeException = (RuntimeException) exCause;
            } else
            {
                throw new RuntimeException(exCause);
            }
            throw runtimeException;
        }
    }


    @Override
    public String toString()
    {
        return toJSON().toString();
    }

    /**
     * 转换为json
     */
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject(5);
        json.put(CODE_FIELD, getIntCode());
        if (code instanceof ResultCode)
        {
            json.put(CODE_NAME_FIELD, ((ResultCode) code).name());
        }
        json.put(DESCRIPTION_FIELD, description);

        Object result = this.result;
        Object extra = this.extra;
        if (result instanceof IObject)
        {
            result = ((IObject) result).toCustomObject();
        }
        if (extra instanceof IObject)
        {
            extra = ((IObject) extra).toCustomObject();
        }

        if (result != null)
        {
            json.put(RESULT_FIELD, result);
        }

        if (extra != null)
        {
            if (extra instanceof JSONObject || extra instanceof JSONArray)
            {
                json.put(EXTRA_FIELD, extra);
            } else
            {
                json.put(EXTRA_FIELD, String.valueOf(extra));
            }
        }


        return json;
    }

    public static JResponse success(Object result)
    {
        JResponse jResponse = new JResponse(ResultCode.SUCCESS);
        jResponse.setResult(result);
        return jResponse;
    }

    public static JResponse failed(String desc)
    {
        return failed(ResultCode.OK_BUT_FAILED, desc);
    }

    public static JResponse failed(ResultCode code, String desc)
    {
        if (code == ResultCode.SUCCESS || code == ResultCode.OK)
        {
            throw new IllegalArgumentException("illegal code:" + code);
        }
        JResponse jResponse = new JResponse(code);
        jResponse.setDescription(desc);
        return jResponse;
    }

}
