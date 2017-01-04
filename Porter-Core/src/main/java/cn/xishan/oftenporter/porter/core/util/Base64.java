package cn.xishan.oftenporter.porter.core.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Base64
{

    public static void encode(int length,InputStream in,OutputStream os) throws IOException
    {
        try
        {
            Base64Util.encode(length,in,os);
        } catch (IOException e)
        {
            throw e;
        }finally
        {
            WPTool.close(in);
            WPTool.close(os);
        }
    }

    public static void decode(InputStream is, BufferedOutputStream bos) throws IOException
    {
        final int EVERY = 4, LEN = 1024;
        byte[] buf = new byte[LEN];
        try
        {
            int n, offset = 0;
            while ((n = is.read(buf, offset, LEN - offset)) != -1)
            {
                n += offset;
                offset = n % EVERY;
                n -= offset;
                bos.write(decode(new String(buf, 0, n)));
                for (int i = 0; i < offset; i++)
                {
                    buf[i] = buf[n + i];
                }
            }
            bos.flush();
        } catch (IOException e)
        {
            throw e;
        } finally
        {
            WPTool.close(bos);
            WPTool.close(is);
        }

    }


    public static byte[] decode(String str)
    {
        return Base64Util.decode(str);
    }

    public static String encode(byte[] data)
    {
        return Base64Util.encode(data);
    }

    private static class Base64Util
    {

        private final static byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                .getBytes();

        private static int[] toInt = new int[128];

        static
        {
            for (int i = 0; i < ALPHABET.length; i++)
            {
                toInt[ALPHABET[i]] = i;
            }
        }

        /**
         * Translates the specified byte array into Base64 string.
         *
         * @param buf the byte array (not null)
         * @return the translated Base64 string (not null)
         */
        public static String encode(byte[] buf)
        {
            int size = buf.length;
            byte[] ar = new byte[((size + 2) / 3) * 4];
            int a = 0;
            int i = 0;
            while (i < size)
            {
                byte b0 = buf[i++];
                byte b1 = (i < size) ? buf[i++] : 0;
                byte b2 = (i < size) ? buf[i++] : 0;

                int mask = 0x3F;
                ar[a++] = ALPHABET[(b0 >> 2) & mask];
                ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
                ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
                ar[a++] = ALPHABET[b2 & mask];
            }
            switch (size % 3)
            {
                case 1:
                    ar[--a] = '=';
                case 2:
                    ar[--a] = '=';
            }
            return new String(ar);
        }


        public static void encode(int size, InputStream in, OutputStream os) throws IOException
        {
            byte[] ar = new byte[4];
            int a = 0;
            int i = 0;
            while (true)
            {
                a = 0;
                byte b0 = (byte) (in.read() & 0xff);
                i++;
                byte b1 = (i < size) ? (byte) (in.read() & 0xff) : 0;
                i++;
                byte b2 = (i < size) ? (byte) (in.read() & 0xff) : 0;
                i++;

                int mask = 0x3F;
                ar[a++] = ALPHABET[(b0 >> 2) & mask];
                ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
                ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
                ar[a++] = ALPHABET[b2 & mask];
                if (i < size)
                {
                    os.write(ar);
                } else
                {
                    break;
                }
            }
            switch (size % 3)
            {
                case 1:
                    ar[--a] = '=';
                case 2:
                    ar[--a] = '=';
            }
            os.write(ar);

        }

        /**
         * Translates the specified Base64 string into a byte array.
         *
         * @param s the Base64 string (not null)
         * @return the byte array (not null)
         */
        public static byte[] decode(String s)
        {
            int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
            byte[] buffer = new byte[s.length() * 3 / 4 - delta];
            int mask = 0xFF;
            int index = 0;
            for (int i = 0; i < s.length(); i += 4)
            {
                int c0 = toInt[s.charAt(i)];
                int c1 = toInt[s.charAt(i + 1)];
                buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
                if (index >= buffer.length)
                {
                    return buffer;
                }
                int c2 = toInt[s.charAt(i + 2)];
                buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
                if (index >= buffer.length)
                {
                    return buffer;
                }
                int c3 = toInt[s.charAt(i + 3)];
                buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
            }
            return buffer;
        }

    }
}
