package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
class IdGenDealt implements AutoSetGen
{
    private static IdGen idGen;

    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option)
    {
        return getDefault();
    }

    static synchronized IdGen getDefault()
    {
        if (idGen != null)
        {
            return idGen;
        }
        long mac = getMac();
        if (mac == -1)
        {
            mac = 0;
        }
        String mchid = IdGen.num10ToNum64(mac);
        idGen = new IdGen(8, 4, "i".toCharArray(), mchid.toCharArray(), IdGen.getDefaultBuilder());
        return idGen;
    }


    private static long getMac()
    {
        long mac = -1;
        try
        {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

            while (enumeration.hasMoreElements())
            {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface != null)
                {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    if (bytes == null)
                    {
                        continue;
                    }
                    long s = BytesTool.readUnShort(bytes, 0);
                    long i = BytesTool.readInt(bytes, 2);
                    long l = (s << 32) | i;
                    if (l > mac)
                    {
                        mac = l;
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return mac;
    }
}
