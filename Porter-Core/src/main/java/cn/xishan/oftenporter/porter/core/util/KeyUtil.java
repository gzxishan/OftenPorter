package cn.xishan.oftenporter.porter.core.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by 刚帅 on 2016/1/14.
 */
public class KeyUtil
{

    public static String randomUUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    public static String secureRandomKey(int length)
    {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[length];
        random.nextBytes(bytes);
        String str1 = HashUtil.sha1(bytes);
        String str2 = UUID.randomUUID().toString();
        return HashUtil.sha1((str1 + str2).getBytes());
    }
}
