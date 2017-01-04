package cn.xishan.oftenporter.servlet;

public enum ContentType
{
    APP_JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    APP_FORM_URLENCODED("application/x-www-form-urlencoded"),

    /**
     * <pre>
     * 1.第1-2个字节表示参数的个数
     * 2.参数部分：1)第1个字节表示名称的长度,2)名称，3)接着两个字节表示内容的长度，4)值，5）重复1）直到读取完所有参数
     * 3.接下来两个字节表示上传WPFormFile文件的个数：
     * 		1）接下来两个字节表示一个json对象的大小（包括文件名name、文件类型type、文件大小size等）;
     * 		2）json内容,3)文件内容。
     * 4.第一个参数值对表示一些说明信息:参数名称内容表示编码方式,如utf-8
     *
     * 注意：所有表示大小的数都数无符号数
     * </pre>
     */
    WPORTER_FORM("wporter/form");
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
