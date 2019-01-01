package cn.xishan.oftenporter.porter.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FileTool
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTool.class);

    /**
     * @param in
     * @param endChar 读到该字符为止
     * @return 不包含endChar
     * @throws IOException
     */
    public static byte[] read(InputStream in, int endChar) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1)
        {
            if (c == endChar)
            {
                break;
            }
            bos.write(c);
        }
        if (c != endChar)
        {
            throw new IOException("illegal end! expect char " + ((char) endChar));
        }
        return bos.toByteArray();
    }

    /**
     * 把内容写到文件中
     *
     * @param content
     * @param encode
     * @param file
     * @param createIfNotExist
     * @throws IOException
     */
    public static void write2File(String content, String encode, File file, boolean createIfNotExist) throws IOException
    {
        write2File(new ByteArrayInputStream(content.getBytes(encode)), file, createIfNotExist);
    }

    public static void write2Stream(String content, String encode, OutputStream os) throws IOException
    {
        try
        {
            in2out(new ByteArrayInputStream(content.getBytes(encode)), os, 2048);
        } finally
        {
            OftenTool.close(os);
        }
    }

    /**
     * 把输入流中的内容写到文件中
     *
     * @param in
     * @param file
     * @param createIfNotExist
     * @throws IOException
     */
    public static void write2File(InputStream in, File file, boolean createIfNotExist) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            if (!file.exists() && createIfNotExist)
            {
                boolean rs = file.createNewFile();
                if (!rs)
                {
                    throw new IOException("create file failed:" + file.getAbsolutePath());
                }
            }
            fos = new FileOutputStream(file);
            byte[] buf = new byte[2048];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                fos.write(buf, 0, n);
            }
            fos.flush();
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            OftenTool.close(fos);
            OftenTool.close(in);
        }
    }

    public static String getString(File file)
    {
        try
        {
            return getString(new FileInputStream(file));
        } catch (IOException e)
        {
            return null;
        }
    }

    /**
     * 读取文件内容到字节数组
     *
     * @param file
     * @param bufSize
     * @return
     * @throws IOException
     */
    public static byte[] getData(File file, int bufSize) throws IOException
    {
        return getData(new FileInputStream(file), bufSize);
    }

    /**
     * 读取文件内容到字节数据
     *
     * @param in
     * @param bufSize
     * @return
     * @throws IOException
     */
    public static byte[] getData(InputStream in, int bufSize) throws IOException
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(in.available());
            byte[] buf = new byte[bufSize];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            OftenTool.close(in);
        }
    }

    public static String getString(File file, String encode) throws IOException
    {
        return getString(file, 2048, encode);
    }

    public static String getString(File file, int bufSize, String encode) throws IOException
    {
        return getString(new FileInputStream(file), bufSize, encode);
    }

    public static String getString(InputStream in, int bufSize, String encode) throws IOException
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(in.available());
            byte[] buf = new byte[bufSize];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                bos.write(buf, 0, n);
            }
            if (encode != null)
            {
                return new String(bos.toByteArray(), encode);
            } else
            {
                return new String(bos.toByteArray(), "utf-8");
            }
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            OftenTool.close(in);
        }
    }

    public static String getString(InputStream in)
    {
        try
        {
            return getString(in, 2048, "utf-8");
        } catch (Exception e)
        {
            return null;
        }

    }

    /**
     * 把输入流写道输出流。
     *
     * @param in
     * @param os
     * @param bufSize
     * @throws IOException
     */
    public static void in2out(InputStream in, OutputStream os, int bufSize) throws IOException
    {
        try
        {
            byte[] buf = new byte[bufSize];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                os.write(buf, 0, n);
            }
            os.flush();
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            OftenTool.close(os);
            OftenTool.close(in);
        }
    }


    /**
     * @param file
     * @return
     * @see #delete(File, boolean)
     */
    public static boolean delete(File file)
    {
        return delete(file, true);
    }

    /**
     * 删除文件或目录。对于目录，进行递归删除。
     *
     * @param file
     * @param includeCurrentDir 如果当前文件为目录的话，是否删除该目录
     * @return
     */
    public static boolean delete(File file, boolean includeCurrentDir)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    if (!delete(f, true))
                    {
                        return false;
                    }
                }
            }
        }
        if (includeCurrentDir)
            return file.delete();

        return true;
    }

    /**
     * 移动文件
     *
     * @param set
     * @param toDir
     */
    public static void moveFiles(Set<File> set, File toDir) throws IOException
    {
        if (set == null)
        {
            return;
        }
        if (toDir.exists() && toDir.isDirectory())
        {
            Iterator<File> iterator = set.iterator();
            while (iterator.hasNext())
            {
                File file = iterator.next();
                moveFile(file, toDir);
            }
        }
    }

    /**
     * 移动文件
     *
     * @param list
     * @param toDir
     */
    public static void moveFiles(List<File> list, File toDir) throws IOException
    {
        if (list == null)
        {
            return;
        }
        if (toDir.exists() && toDir.isDirectory())
        {
            for (int i = 0; i < list.size(); i++)
            {
                File file = list.get(i);
                moveFile(file, toDir);
            }
        }
    }

    /**
     * 移动文件，如果目标文件夹已经存在相应文件名的文件，则直接返回.
     *
     * @param file
     * @param toDir
     */
    public static void moveFile(File file, File toDir) throws IOException
    {
        if (toDir.exists() && toDir.isDirectory() && file.exists() && !file.isDirectory())
        {
            File desFile = new File(toDir.getPath() + File.separator + file.getName());
            if (desFile.exists())
            {
                return;
            }
            FileTool.write2File(new FileInputStream(file), desFile, true);
            boolean rs = file.delete();
            if (!rs)
            {
                LOGGER.warn("delete source file failed:{}", file);
            }
        }
    }
}
