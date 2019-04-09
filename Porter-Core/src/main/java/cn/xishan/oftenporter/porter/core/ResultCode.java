package cn.xishan.oftenporter.porter.core;

/**
 * 对返回码的定义
 *
 * @author Administrator
 */
public enum ResultCode
{
    /**
     * 响应成功，将得到请求的内容。
     */
    SUCCESS(0),

    /**
     * 同{@linkplain #SUCCESS}
     */
    OK(0),

    /**
     * 用户友好的错误，描述信息可以直接弹出给用户
     */
    USER_FRIENDLY_ERROR(4004),

    /**
     * 无访问权限。
     */
    ACCESS_DENIED(HttpStatusCode.UNAUTHORIZED),

    /**
     * 访问资源不可用
     */
    NOT_AVAILABLE(HttpStatusCode.NOT_FOUND),

    /**
     * 服务器发生异常
     */
    SERVER_EXCEPTION(-3),

    /**
     * 响应成功，但是操作失败
     */
    OK_BUT_FAILED(-4),

    /**
     * 同{@linkplain #OK_BUT_FAILED}
     */
    FAILED(-4),

    /**
     * 处理参数错误
     */
    PARAM_DEAL_EXCEPTION(-6),

    /**
     * 异常
     */
    EXCEPTION(-7),

    /**
     * 数据库异常
     */
    DB_EXCEPTION(-8),

    /**
     * 调用接口函数错误
     */
    INVOKE_METHOD_EXCEPTION(-9),
    /**
     * 网络错误
     */
    NET_EXCEPTION(-10),
    /**
     * 调用第三方接口错误
     */
    OTHER_PORTER_EXCEPTION(-11),
    /**
     * 未登陆
     */
    NOT_LOGIN(-12),
    /**
     * 需要登陆
     */
    NEED_LOGIN(-13),
    /**
     * 重定向
     */
    REDIRECT(-14),
    Other(-1);

    private int code;

    ResultCode(int code)
    {
        this.code = code;
    }

    /**
     * 转换为结果码。
     *
     * @return 整型的结果码
     */
    public int toCode()
    {
        return code;
    }

    public static ResultCode toResponseCode(int code)
    {
        ResultCode responseCode;
        switch (code)
        {
            case 0:
                responseCode = SUCCESS;
                break;
            case HttpStatusCode.NOT_FOUND:
                responseCode = NOT_AVAILABLE;
                break;
            case 4004:
                responseCode = USER_FRIENDLY_ERROR;
                break;
            case -1:
                responseCode = Other;
                break;
            case -3:
                responseCode = SERVER_EXCEPTION;
                break;
            case -4:
                responseCode = OK_BUT_FAILED;
                break;
            case HttpStatusCode.UNAUTHORIZED:
                responseCode = ACCESS_DENIED;
                break;
            case -6:
                responseCode = PARAM_DEAL_EXCEPTION;
                break;
            case -7:
                responseCode = EXCEPTION;
                break;
            case -8:
                responseCode = DB_EXCEPTION;
                break;
            case -9:
                responseCode = INVOKE_METHOD_EXCEPTION;
                break;
            case -10:
                responseCode = NET_EXCEPTION;
                break;
            case -11:
                responseCode = OTHER_PORTER_EXCEPTION;
                break;
            case -12:
                responseCode = NOT_LOGIN;
                break;
            case -13:
                responseCode = NEED_LOGIN;
                break;
            case -14:
                responseCode = REDIRECT;
                break;
            default:
                //throw new RuntimeException("unknown code:" + code);
                responseCode = null;
        }
        return responseCode;
    }
}
