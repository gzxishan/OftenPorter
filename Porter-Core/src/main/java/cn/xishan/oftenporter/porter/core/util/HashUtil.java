package cn.xishan.oftenporter.porter.core.util;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil
{

    public static String md5(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "MD5", offsetAndLength);
    }

    public static String md5(InputStream in, int bufSize)
    {
        return hashHex("MD5", in, bufSize);
    }


    /**
     * 进行md5编码，最后截取第9到24个字符(16)
     */
    public static String md5_16(byte[] bytes, int... offsetAndLength)
    {
        String md5Str = md5(bytes, offsetAndLength);
        return md5Str.substring(8, 24);
    }

    public static String md5_16(InputStream in, int bufSize)
    {
        String md5Str = md5(in, bufSize);
        return md5Str.substring(8, 24);
    }

    public static String sha1(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-1", offsetAndLength);
    }

    public static String sha1(InputStream in, int bufSize)
    {
        return hashHex("sha-1", in, bufSize);
    }

    public static String sha256(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-256", offsetAndLength);
    }

    public static String sha256(InputStream in, int bufSize)
    {
        return hashHex("sha-256", in, bufSize);
    }

    public static String sha384(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-384", offsetAndLength);
    }

    public static String sha384(InputStream in, int bufSize)
    {
        return hashHex("sha-384", in, bufSize);
    }

    public static String sha512(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-512", offsetAndLength);
    }

    public static String sha512(InputStream in, int bufSize)
    {
        return hashHex("sha-512", in, bufSize);
    }


    /**
     * @param bs              待hash的数据
     * @param sname           hash算法的名称
     * @param offsetAndLength 大小为0或2
     * @return 返回16进制hash值(小写)
     */
    public static String hashHex(byte[] bs, String sname, int... offsetAndLength)
    {
        byte[] bytes = hash(bs, sname, offsetAndLength);
        return BytesTool.toHex(bytes, 0, bytes.length, true);
    }


    /**
     * @param sname hash算法的名称
     * @param in
     * @return 返回16进制hash值(小写)
     */
    public static String hashHex(String sname, InputStream in, int bufSize)
    {
        byte[] bytes = hash(sname, in, bufSize);
        return BytesTool.toHex(bytes, 0, bytes.length, true);
    }

    /**
     * @param bs              待hash的数据
     * @param sname           hash算法的名称,如：MD5,sha-1,sha-256,sha-384,sha-512
     * @param offsetAndLength 大小为0或2
     * @return 返回hash值
     */
    public static byte[] hash(byte[] bs, String sname, int... offsetAndLength)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(sname);
            if (offsetAndLength.length > 0)
            {
                digest.update(bs, offsetAndLength[0], offsetAndLength[1]);
            } else
            {
                digest.update(bs, 0, bs.length);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param bs              待hash的数据
     * @param sname           hash算法的名称
     * @param offsetAndLength 大小为0或2
     * @return 返回hash值
     */
    /**
     * @param sname   hash算法的名称,如：MD5,sha-1,sha-256,sha-384,sha-512
     * @param in      待hash的数据流
     * @param bufSize
     * @return
     */
    public static byte[] hash(String sname, InputStream in, int bufSize)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(sname);
            byte[] buf = new byte[bufSize];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                digest.update(buf, 0, n);
            }
            return digest.digest();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            WPTool.close(in);
        }
    }


}