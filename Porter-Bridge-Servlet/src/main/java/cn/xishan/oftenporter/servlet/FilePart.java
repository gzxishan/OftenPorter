package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.util.FileTool;

import java.io.File;

/**
 * 使用完成后需要调用{@linkplain #delete()}。
 *
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
    private Object ref;

    public FilePart(String[] originalNames, File[] files, Object ref)
    {
        this.originalName = originalNames[0];
        this.file = files[0];
        this.originalNames = originalNames;
        this.files = files;
        this.ref = ref;
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
        ref = null;
        for (File file : files)
        {
            FileTool.delete(file);
        }
    }
}
