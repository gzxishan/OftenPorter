package cn.xishan.oftenporter.porter.core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil
{

    public static String md5(byte[] bytes)
    {
        return md5(bytes, 0, bytes.length);
    }

    public static String md5(byte[] bytes, int offset, int length)
    {
        return hashHex("MD5", new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
    }

    public static String md5(InputStream in, int bufSize)
    {
        return hashHex("MD5", in, bufSize);
    }


    /**
     * 见{@linkplain #md5(InputStream, int)}
     */
    public static String md5_16(byte[] bytes)
    {
        return md5_16(bytes, 0, bytes.length);
    }

    /**
     * 进行md5编码，最后截取第9到24个字符(16)
     */
    public static String md5_16(byte[] bytes, int offset, int length)
    {
        String md5Str = md5(new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
        return md5Str.substring(8, 24);
    }

    public static String md5_16(InputStream in, int bufSize)
    {
        String md5Str = md5(in, bufSize);
        return md5Str.substring(8, 24);
    }

    public static String sha1(byte[] bytes)
    {
        return sha1(bytes, 0, bytes.length);
    }

    public static String sha1(byte[] bytes, int offset, int length)
    {
        return hashHex("sha-1", new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
    }

    public static String sha1(InputStream in, int bufSize)
    {
        return hashHex("sha-1", in, bufSize);
    }

    public static String sha256(byte[] bytes)
    {
        return sha256(bytes, 0, bytes.length);
    }

    public static String sha256(byte[] bytes, int offset, int length)
    {
        return hashHex("sha-256", new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
    }

    public static String sha256(InputStream in, int bufSize)
    {
        return hashHex("sha-256", in, bufSize);
    }

    public static String sha384(byte[] bytes)
    {
        return sha384(bytes, 0, bytes.length);
    }

    public static String sha384(byte[] bytes, int offset, int length)
    {
        return hashHex("sha-384", new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
    }

    public static String sha384(InputStream in, int bufSize)
    {
        return hashHex("sha-384", in, bufSize);
    }

    public static String sha512(byte[] bytes)
    {
        return sha512(bytes, 0, bytes.length);
    }

    public static String sha512(byte[] bytes, int offset, int length)
    {
        return hashHex("sha-512", new ByteArrayInputStream(bytes, offset, length), length / 2 + 1);
    }

    public static String sha512(InputStream in, int bufSize)
    {
        return hashHex("sha-512", in, bufSize);
    }


    /**
     * @param sname hash算法的名称
     * @param bytes 待hash的数据
     * @return 返回16进制hash值(小写)
     */
    public static String hashHex(String sname, byte[] bytes)
    {
        byte[] rs = hash(sname, bytes);
        return BytesTool.toHex(rs, 0, rs.length, true);
    }


    /**
     * @param sname hash算法的名称
     * @param in
     * @return 返回16进制hash值(小写)
     */
    public static String hashHex(String sname, InputStream in, int bufSize)
    {
        byte[] bytes = hash(sname, bufSize, in);
        return BytesTool.toHex(bytes, 0, bytes.length, true);
    }

    /**
     * @param sname  hash算法的名称,如：MD5,sha-1,sha-256,sha-384,sha-512
     * @param inputs 待hash的数据
     * @return 返回hash值
     */
    public static byte[] hash(String sname, byte[]... inputs)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(sname);
            for (byte[] bs : inputs)
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
     * @param bufSize
     * @param inputs  待hash的数据流
     * @return
     */
    public static byte[] hash(String sname, int bufSize, InputStream... inputs)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(sname);
            byte[] buf = new byte[bufSize];
            int n;
            for (InputStream in : inputs)
            {
                while ((n = in.read(buf)) != -1)
                {
                    digest.update(buf, 0, n);
                }
            }
            return digest.digest();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            for (InputStream in : inputs)
            {
                WPTool.close(in);
            }
        }
    }


}