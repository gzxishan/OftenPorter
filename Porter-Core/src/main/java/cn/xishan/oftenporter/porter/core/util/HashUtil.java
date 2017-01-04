package cn.xishan.oftenporter.porter.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil
{

    public static String md5(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "MD5", offsetAndLength);
    }


    /**
     * 进行md5编码，最后截取第9到24个字符(16)
     */
    public static String md5_16(byte[] bytes, int... offsetAndLength)
    {
        String md5Str = md5(bytes, offsetAndLength);
        return md5Str.substring(8, 24);
    }

    public static String sha1(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-1", offsetAndLength);
    }

    public static String sha256(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-256", offsetAndLength);
    }

    public static String sha384(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-384", offsetAndLength);
    }

    public static String sha512(byte[] bytes, int... offsetAndLength)
    {
        return hashHex(bytes, "sha-512", offsetAndLength);
    }


    /**
     * @param bs              待hash的数据
     * @param sname           hash算法的名称
     * @param offsetAndLength 大小为0或2
     * @return 返回16进制hash值
     */
    public static String hashHex(byte[] bs, String sname, int... offsetAndLength)
    {
        byte[] bytes = hash(bs, sname, offsetAndLength);
        return BytesTool.toHex(bytes, 0, bytes.length);
    }

    /**
     * @param bs              待hash的数据
     * @param sname           hash算法的名称
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


}