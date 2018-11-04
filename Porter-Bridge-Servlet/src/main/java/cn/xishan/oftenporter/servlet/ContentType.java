package cn.xishan.oftenporter.servlet;

public enum ContentType
{
    APP_JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    APP_FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM("multipart/form-data");
    private String type;

    /**
     * 头的名称
     */
    public static final String HEADER_NAME = "Content-Type";

    ContentType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }
}
