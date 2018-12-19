package cn.xishan.oftenporter.servlet;

import java.io.File;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
public class FilePart implements AutoCloseable
{
    /**
     * 原始文件名。
     */
    public String originalName;
    public File file;

    public String[] originalNames;
    public File[] files;

    public FilePart(String[] originalNames, File[] files)
    {
        this.originalName = originalNames[0];
        this.file = files[0];
        this.originalNames = originalNames;
        this.files = files;
    }

    /**
     * 删除所有上传的文件。
     */
    public void delete()
    {
        close();
    }

    @Override
    public void close()
    {
        for (File file : files)
        {
            file.delete();
        }
    }
}
