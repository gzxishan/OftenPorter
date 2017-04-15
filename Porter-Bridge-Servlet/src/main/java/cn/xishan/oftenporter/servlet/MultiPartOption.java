package cn.xishan.oftenporter.servlet;

/**
 * 上传的文件最后变成{@linkplain FilePart}对象。
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
        this.tempDir = tempDir;
        this.maxBytesEachFile = maxBytesEachFile;
        this.totalBytes = totalBytes;
    }
}
