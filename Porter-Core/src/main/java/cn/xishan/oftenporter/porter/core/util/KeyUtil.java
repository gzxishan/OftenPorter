package cn.xishan.oftenporter.porter.core.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by 刚帅 on 2016/1/14.
 */
public class KeyUtil
{

    /**
     * 长度为32位的uuid字符串（没有“-”连接符）。
     * @return
     */
    public static String randomUUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    /**
     * 长度48的key。
     * @return
     */
    public static String random48Key(){
        char[] cs = new char[48];
        String uuid = randomUUID();
        String md5_16=HashUtil.md5_16(randomUUID().getBytes());
        int minLen=md5_16.length();
        for (int i = 0,j=0,k=0; k < cs.length;k++)
        {
            if(i<j||j>=minLen){
                cs[k]=uuid.charAt(i++);
            }else{
                cs[k]=md5_16.charAt(j++);
            }

        }

        return new String(cs);
    }

    public static String secureRandomKeySha256(int initLength)
    {
        byte bytes[] = secureRandomKeyBytes(initLength);
        String str1 = HashUtil.sha1(bytes);
        String str2 = UUID.randomUUID().toString();
        return HashUtil.sha256((str1 + str2).getBytes());
    }

    public static byte[] secureRandomKeyBytes(int length)
    {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[length];
        random.nextBytes(bytes);
       return bytes;
    }
}
