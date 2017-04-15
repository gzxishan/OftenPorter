package cn.xishan.oftenporter.servlet;

import java.io.File;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
public class FilePart
{
    /**
     * 原始文件名。
     */
    public String originalName;
    public File file;

    public FilePart(String originalName, File file)
    {
        this.originalName = originalName;
        this.file = file;
    }
}
