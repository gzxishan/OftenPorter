package cn.xishan.oftenporter.oftendb.jbatis;

/**
 * Created by chenyg on 2017-04-29.
 */
public class JDaoOption
{
    /**
     * 文件存放的class路径，以"/"分隔，为空时，表示为对象的路径。
     */
    public String classpath;

    /**
     * 用于存放js的文件路径,优先于{@linkplain #classpath}，每次调用时去获取对应js(如果文件改变)。
     */
    public String debugDirPath;


    /**
     * 待注入的脚本。
     */
    public String injectScript;

    /**
     * 表名前缀。
     */
    public String tableNamePrefix = "";

    public String scriptEncoding = "utf-8";
}
