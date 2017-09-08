package cn.xishan.oftenporter.porter.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by https://github.com/CLovinr on 2017/8/17.
 */
public class BytesToolTest {


    @Test
    public void testToSignShort(){
        int n = Integer.parseInt("1000000000000001",2);
        assertEquals(-1,BytesTool.toSignShort(n));
        assertEquals(110,BytesTool.toSignShort(110));
        byte[] bs = new byte[2];
        BytesTool.writeShortLittleEndian(bs,0,-110);
        assertEquals(-110,BytesTool.toJavaSignShort(BytesTool.readUnShortLittleEndian(bs,0)));

    }


    @Test
    public void testInt() {
        int n = 0x89_9a_ab_bc;
        byte[] bs = new byte[4];

        //大端测试
        BytesTool.writeInt(bs, 0, n);
        assertEquals("899aabbc", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readInt(bs, 0));

        BytesTool.writeIntBigEndian(bs, 0, n);
        assertEquals("899AABBC", BytesTool.toHexUppercase(bs));
        assertEquals(n, BytesTool.readIntBigEndian(bs, 0));

        /////////////
        //小端测试
        BytesTool.writeIntLittleEndian(bs, 0, n);
        assertEquals("bcab9a89", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readIntLittleEndian(bs, 0));

    }


    @Test
    public void testLong() {
        long n = 0x89_9a_ab_bc_12345678L;
        byte[] bs = new byte[8];

        //大端测试
        BytesTool.writeLong(bs, 0, n);
        assertEquals("899aabbc12345678", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readLong(bs, 0));

        BytesTool.writeLongBigEndian(bs, 0, n);
        assertEquals("899AABBC12345678", BytesTool.toHexUppercase(bs));
        assertEquals(n, BytesTool.readLongBigEndian(bs, 0));

        /////////////
        //小端测试
        BytesTool.writeLongLittleEndian(bs, 0, n);
        assertEquals("78563412bcab9a89", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readLongLittleEndian(bs, 0));

    }

    @Test
    public void testShort() {
        int n =  0x89_12;
        byte[] bs = new byte[2];

        //大端测试
        BytesTool.writeShort(bs, 0, n);
        assertEquals("8912", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readUnShort(bs, 0));

        BytesTool.writeShortBigEndian(bs, 0, n);
        assertEquals("8912", BytesTool.toHexUppercase(bs));
        assertEquals(n, BytesTool.readUnShortBigEndian(bs, 0));

        /////////////
        //小端测试
        BytesTool.writeShortLittleEndian(bs, 0, n);
        assertEquals("1289", BytesTool.toHex(bs));
        assertEquals(n, BytesTool.readUnShortLittleEndian(bs, 0));

    }

}
