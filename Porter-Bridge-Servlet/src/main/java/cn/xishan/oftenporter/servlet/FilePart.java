package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.FileTool;

import java.io.File;
import java.util.List;

/**
 * 使用完成后需要调用{@linkplain #delete()}。
 *
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
public class FilePart implements AutoCloseable
{
    private static final String ATTR_NAME = "cn.xishan.oftenporter.servlet.__FILE_PART_LIST__";

    /**
     * 原始文件名。
     */
    public String originalName;
    public File file;

    public String[] originalNames;
    public File[] files;
    private Object ref;

    private boolean deleteAfterInvoke = true;

    public FilePart(String[] originalNames, File[] files, Object ref)
    {
        this.originalName = originalNames[0];
        this.file = files[0];
        this.originalNames = originalNames;
        this.files = files;
        this.ref = ref;
    }

    public boolean isDeleteAfterInvoke()
    {
        return deleteAfterInvoke;
    }

    public void setDeleteAfterInvoke(boolean deleteAfterInvoke)
    {
        this.deleteAfterInvoke = deleteAfterInvoke;
    }

    /**
     * 删除所有上传的文件。
     */
    public void delete()
    {
        close();
    }

    /**
     * 删除当前请求涉及的所有临时文件。
     *
     * @param oftenObject
     */
    public static void clear(OftenObject oftenObject)
    {
        List<FilePart> fileParts = oftenObject.removeRequestData(ATTR_NAME);
        if (fileParts != null)
        {
            for (FilePart filePart : fileParts)
            {
                filePart.delete();
            }
        }
    }

    static void bindData(OftenObject oftenObject, List<FilePart> fileParts)
    {
        oftenObject.putRequestData(ATTR_NAME, fileParts);
        oftenObject.addListener(new OftenObject.IFinalListener()
        {
            @Override
            public void beforeFinal(OftenObject oftenObject) throws Throwable
            {
                List<FilePart> fileParts = oftenObject.removeRequestData(ATTR_NAME);
                if (fileParts != null)
                {
                    for (FilePart filePart : fileParts)
                    {
                        if (filePart.isDeleteAfterInvoke())
                        {
                            filePart.delete();
                        }
                    }
                }
            }

            @Override
            public void onFinalException(OftenObject oftenObject, Throwable throwable) throws Throwable
            {

            }

            @Override
            public void afterFinal(OftenObject oftenObject) throws Throwable
            {

            }
        });
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
