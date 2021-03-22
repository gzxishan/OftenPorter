package cn.xishan.oftenporter.servlet;

import java.io.File;

/**
 * 上传的文件最后变成{@linkplain FilePart}对象。
 * <p>
 * <strong>注意</strong>：使用{@linkplain IgnoreDefaultMultipart}来忽略该处理。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
public class MultiPartOption
{
    public String tempDir;
    public int cacheSize = 1024 * 5;
    public int maxBytesEachFile;
    public int totalBytes;

    public MultiPartOption(String tempDir, int maxBytesEachFile, int totalBytes)
    {
        if (tempDir == null)
        {
            tempDir = System.getProperty("java.io.tmpdir");
        }
        this.tempDir = tempDir.replace(File.separatorChar, '/');
        try
        {
            File dir = new File(tempDir);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        this.maxBytesEachFile = maxBytesEachFile;
        this.totalBytes = totalBytes;
    }
}
