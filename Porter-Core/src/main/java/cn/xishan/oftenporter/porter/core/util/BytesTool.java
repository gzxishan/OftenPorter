package cn.xishan.oftenporter.porter.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BytesTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytesTool.class);

    /**
     * @param buffer 尽可能读满
     * @param in     不会调用close
     * @return 返回传人的buffer
     * @throws IOException
     */
    public static ByteBuffer read(ByteBuffer buffer, InputStream in) throws IOException {
        buffer.clear();
        byte[] bs = buffer.array();
        int n;
        n = in.read(bs);
        buffer.position(0);
        buffer.limit(n == -1 ? 0 : n);
        return buffer;
    }


    public static String toMinLengthOfLeft(String content, int length, char c) {
        return toMinLength(content, length, c, true);
    }

    public static String toMinLengthOfRight(String content, int length, char c) {
        return toMinLength(content, length, c, false);
    }

    /**
     * 当content小于length时，则在左或右补字符c。
     *
     * @param content
     * @param length
     * @param c
     * @param isLeft
     */
    public static String toMinLength(String content, int length, char c, boolean isLeft) {
        for (int i = length - content.length(); i > 0; i--) {
            if (isLeft) {
                content = c + content;
            } else {
                content += c;
            }
        }
        return content;
    }

    /**
     * 至少读取指定的字节数
     *
     * @param buf
     * @param offset
     * @param needLength 至少读取的字节数
     * @param in
     * @throws IOException
     */
    public static void readLength(byte[] buf, int offset, int needLength, InputStream in) throws IOException {
        int n;
        while (needLength > 0) {
            n = in.read(buf, offset, needLength);
            if (n == -1) {
                throw new IOException("the stream is end!");
            }
            needLength -= n;
            offset += n;
        }
    }

    /**
     * 转换成16进制,小写.
     *
     * @param bs
     * @return
     */
    public static String toHex(byte[] bs) {
        return toHex(bs, 0, bs.length, true);
    }

    /**
     * 转换成16进制,大写.
     *
     * @param bs
     * @return
     */
    public static String toHexUppercase(byte[] bs) {
        return toHex(bs, 0, bs.length, false);
    }


    /**
     * 转换成16进制.
     *
     * @param bs
     * @param offset
     * @param length
     * @param isLowercase 是否是小写
     * @return
     */
    public static String toHex(byte[] bs, int offset, int length, boolean isLowercase) {
        final String HEX = isLowercase ? "0123456789abcdef" : "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(length * 2);
        int nend = offset + length;
        for (int i = offset; i < nend; i++) {
            byte b = bs[i];
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
        }

        return sb.toString();
    }

    /**
     * 转换成字节
     *
     * @param str
     * @return
     */
    public static byte[] hexToByte(String str) {
        byte[] bs = new byte[str.length() / 2];
        int index, n;
        for (int i = 0; i < bs.length; i++) {
            index = i << 1;
            n = Integer.parseInt(str.substring(index, index + 2), 16);
            bs[i] = (byte) n;
        }
        return bs;
    }


    /**
     * @see #combineBytes(boolean, byte[]...)
     */
    public static byte[][] uncombineBytes(boolean intOrShortLen, byte[] bss) {
        ArrayList<byte[]> list = new ArrayList<>();
        for (int i = 0; i < bss.length; ) {
            int n;
            if (intOrShortLen) {
                n = readInt(bss, i);
                i += 4;
            } else {
                n = readUnShort(bss, i);
                i += 2;
            }

            byte[] bs = new byte[n];
            System.arraycopy(bss, i, bs, 0, n);
            i += n;
            list.add(bs);
        }
        return list.toArray(new byte[0][]);
    }

    /**
     * 格式：【int/short】【byte[]】...
     */
    public static byte[] combineBytes(boolean intOrShortLen, byte[]... bss) {
        int n = 0;
        for (int i = 0; i < bss.length; i++) {
            n += 4 + bss[i].length;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(n);

        try {
            if (intOrShortLen) {
                byte[] temp = new byte[4];
                for (int i = 0; i < bss.length; i++) {
                    writeInt(temp, 0, bss[i].length);
                    bos.write(temp);
                    bos.write(bss[i]);
                }
            } else {
                byte[] temp = new byte[2];
                for (int i = 0; i < bss.length; i++) {
                    writeShort(temp, 0, bss[i].length);
                    bos.write(temp);
                    bos.write(bss[i]);
                }
            }

        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return bos.toByteArray();
    }

    /**
     * 比较两个字节数组是否相等
     *
     * @param bs1
     * @param offset1
     * @param length
     * @param bs2
     * @param offset2
     * @return
     */
    public static boolean eq(byte[] bs1, int offset1, int length, byte[] bs2, int offset2) {
        if (length > bs2.length - offset2) {
            return false;
        }

        boolean isEq = true;
        for (int i = 0; i < length; i++) {
            if (bs1[offset1 + i] != bs2[offset2 + i]) {
                isEq = false;
                break;
            }
        }
        return isEq;
    }

    /**
     * 写入一个整数到字节数组中,大端，高字节在低索引位置。
     *
     * @param data
     * @param offset 开始的索引
     * @param n
     */
    public static void writeInt(byte[] data, int offset, int n) {
        writeIntBigEndian(data, offset, n);
    }


    public static void writeIntBigEndian(byte[] data, int offset, int n) {

        data[offset] = (byte) ((n >> 24) & 0xFF);
        data[offset + 1] = (byte) ((n >> 16) & 0xFF);
        data[offset + 2] = (byte) ((n >> 8) & 0xFF);
        data[offset + 3] = (byte) (n & 0xFF);
    }

    public static void writeIntLittleEndian(byte[] data, int offset, int n) {

        data[offset + 3] = (byte) ((n >> 24) & 0xFF);
        data[offset + 2] = (byte) ((n >> 16) & 0xFF);
        data[offset + 1] = (byte) ((n >> 8) & 0xFF);
        data[offset] = (byte) (n & 0xFF);
    }

    /**
     * 从索引开始的四个字节中读取一个int整数,大端模式,高字节在低索引位置。
     *
     * @param data
     * @param offset
     * @return
     */
    public static int readInt(byte[] data, int offset) {
        return readIntBigEndian(data, offset);
    }

    public static int readIntBigEndian(byte[] data, int offset) {

        int n = ((data[offset + 3] & 0xFF) | ((data[offset + 2] & 0xFF) << 8)
                | ((data[offset + 1] & 0xFF) << 16) | ((data[offset] & 0xFF) << 24));
        return n;
    }

    public static int readIntLittleEndian(byte[] data, int offset) {
        int n = ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24));
        return n;
    }


    /**
     * 大端.
     *
     * @param bs
     * @param offset
     * @param l
     */
    public static void writeLong(byte[] bs, int offset, long l) {
        writeLongBigEndian(bs, offset, l);
    }

    public static void writeLongBigEndian(byte[] bs, int offset, long l) {
        writeIntBigEndian(bs, offset, (int) ((l >>> 32) & 0xffffffff));
        writeIntBigEndian(bs, offset + 4, (int) (l & 0xffffffff));
    }

    public static void writeLongLittleEndian(byte[] bs, int offset, long l) {
        writeIntLittleEndian(bs, offset + 4, (int) ((l >>> 32) & 0xffffffff));
        writeIntLittleEndian(bs, offset, (int) (l & 0xffffffff));
    }

    /**
     * 大端
     *
     * @param data
     * @param offset
     * @return
     */
    public static long readLong(byte[] data, int offset) {
        return readLongBigEndian(data, offset);
    }

    public static long readLongBigEndian(byte[] data, int offset) {
        long l = ((long) readInt(data, offset)) << 32;
        l |= readInt(data, offset + 4);
        return l;
    }

    public static long readLongLittleEndian(byte[] data, int offset) {
        long l = ((long) readIntLittleEndian(data, offset + 4)) << 32;
        l |= readIntLittleEndian(data, offset);
        return l;
    }

    /**
     * 写入一个short到字节数组中,大端，高字节在低索引位置,高字节的第一位是符号位。
     *
     * @param data
     * @param offset 开始的索引
     * @param n
     */
    public static void writeShort(byte[] data, int offset, int n) {
        writeShortBigEndian(data, offset, n);
    }

    public static void writeShortBigEndian(byte[] data, int offset, int n) {
        //n = int2SignShort(n);
        data[offset] = (byte) ((n >> 8) & 0xFF);
        data[offset + 1] = (byte) (n & 0xFF);
    }

//    private static int int2SignShort(int n) {
//        int m;
//        if (n > 0) {
//            m = n & 0xFFFF;
//        } else {
//            m = (-n)& 0xFFFF;
//            if ((m & MASK_SHORT_SIGN) < 0) {
//                m += MASK_SHORT_SIGN;
//            }
//        }
//        return m;
//    }

    public static void writeShortLittleEndian(byte[] data, int offset, int n) {
        //n = int2SignShort(n);
        data[offset] = (byte) (n & 0xFF);
        data[offset + 1] = (byte) ((n >> 8) & 0xFF);
    }

    /**
     * 从索引开始的2个字节中读取一个short,大端.
     *
     * @param data
     * @param offset
     * @return
     */
    public static int readUnShort(byte[] data, int offset) {
        return readUnShortBigEndian(data, offset);
    }

    private static final int MASK_SHORT_SIGN = 1 << 15;
    private static final int MASK_SHORT_VALUE = 0xFFFF ^ MASK_SHORT_SIGN;

    /**
     * 转换成有符号的short值，[sbbb bbbb][bbbb bbbb],s为0表示正数、为1表示负数
     *
     * @param shortValue
     * @return
     */
    public static int toSignShort(int shortValue) {
        shortValue &= 0xffff;
        boolean sign = (MASK_SHORT_SIGN & shortValue) > 0;
        int value = shortValue & MASK_SHORT_VALUE;
        return sign ? -value : value;
    }

    /**
     * 转换成有符号的short值，
     *
     * @param shortValue
     * @return
     */
    public static short toJavaSignShort(int shortValue) {
        short s = (short) shortValue;
        return s;
    }

    public static int readUnShortBigEndian(byte[] data, int offset) {
        int n = ((data[offset + 1] & 0xFF)) | ((data[offset] & 0xFF) << 8);
        return n;
    }

    public static int readUnShortLittleEndian(byte[] data, int offset) {
        int n = ((data[offset] & 0xFF)) | ((data[offset + 1] & 0xFF) << 8);
        return n;
    }

}
