package cn.xishan.oftenporter.porter.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/9.
 */
public class OftenToolTest
{
    @Test
    public void testIsEmpty()
    {
        Assert.assertTrue(OftenTool.isEmpty(null));
        Assert.assertTrue(OftenTool.isEmpty(""));
        Assert.assertTrue(OftenTool.isEmpty(new StringBuilder()));
        Assert.assertFalse(OftenTool.isNullOrEmptyCharSequence(new Object()));

        Assert.assertTrue(OftenTool.isEmptyOf(new ArrayList<>()));
        Assert.assertTrue(OftenTool.isEmptyOf(new HashSet<>()));
        Assert.assertTrue(OftenTool.isEmptyOf(new HashMap<>()));

        Assert.assertTrue(OftenTool.isEmptyOf(new Object[0]));
//        Assert.assertTrue(OftenTool.isEmptyOf(new int[0]));
        Assert.assertTrue(OftenTool.isEmptyOf(new Object[0][0]));
    }


    interface A
    {
        void a1();

        void a2();
    }

    interface A_1 extends A
    {
        @Override
        void a1();
    }

    class C implements A
    {

        @Override
        public void a1()
        {

        }

        @Override
        public void a2()
        {

        }
    }

    class C2 implements A_1, A
    {

        @Override
        public void a2()
        {

        }

        @Override
        public void a1()
        {

        }
    }


    abstract class C3 implements A_1, A
    {

    }

    interface B1
    {
        void b1();
    }

    interface B2
    {
        void b2();
    }

    interface B3 extends B1, B2
    {

    }

    @Test
    public void testGetAllMethods()
    {
        Method[] methods1 = OftenTool.getAllMethods(A.class);
        print(methods1);

        Method[] methods2 = OftenTool.getAllMethods(A_1.class);
        print(methods2);

        Method[] methods3 = OftenTool.getAllMethods(C.class);
        print(methods3);

        Method[] methods4 = OftenTool.getAllMethods(C2.class);
        print(methods4);

        Method[] methods5 = OftenTool.getAllMethods(C3.class);
        print(methods5);

        Method[] methods6 = OftenTool.getAllMethods(B3.class);
        print(methods6);
    }

    static void print(Method[] methods)
    {
        for (Method method : methods)
        {
            if (method.getDeclaringClass().equals(Object.class))
            {
                continue;
            }
            System.out.print(method.getDeclaringClass().getSimpleName() + "." + method.getName() + " ");
        }
        System.out.println();
    }

    @Test
    public void testIsAssignable()
    {
        Assert.assertTrue(OftenTool.isAssignable(Object.class, Object.class));
        Assert.assertTrue(OftenTool.isAssignable(ArrayList.class, Collection.class));
        Assert.assertTrue(OftenTool.isAssignableForOneOf(String[].class, Object[].class));
    }

    @Test
    public void testGetObjectAttr()
    {
        JSONObject jsonObject = JSON.parseObject("{" +
                "a:{" +
                "b:{c:'C',d:100,e:true,f:null,g:'',date:'2020-11-03'}" +
                "}," +
                "x:[]," +
                "y:{}" +
                "}");

        Assert.assertEquals(100, OftenTool.getObjectAttrInt(jsonObject, "a.b.d"));
        Assert.assertEquals("C", OftenTool.getObjectAttrString(jsonObject, "a.b.c"));
        Assert.assertTrue(OftenTool.getObjectAttrBoolean(jsonObject, "a.b.e"));
        Assert.assertNull(OftenTool.getObjectAttr(jsonObject, "a.e.d"));
        Assert.assertNull(OftenTool.getObjectAttr(jsonObject, "a.b.f"));

        Assert.assertEquals("",OftenTool.getObjectAttrString(jsonObject, "a.b.g"));
        Assert.assertEquals("", OftenTool.getObjectAttr(jsonObject, "a.b.g"));
        Assert.assertEquals("2020-11-03",
                new SimpleDateFormat("yyyy-MM-dd").format(OftenTool.getObjectAttrDate(jsonObject, "a.b.date")));


        OftenTool.getObjectAttrArray(jsonObject, "x");
        OftenTool.getObjectAttrJSON(jsonObject, "y");

        jsonObject = JSON.parseObject("{a:{b:'C'},'a.b':'C2'}");
        Assert.assertEquals("C", OftenTool.getObjectAttrString(jsonObject, "a.b"));

        Object pre = OftenTool.setObjectAttr(jsonObject, "a.b", "B");
        Assert.assertEquals("C", pre);
        Assert.assertEquals("B", OftenTool.getObjectAttrString(jsonObject, "a.b"));
        Assert.assertEquals("C2", jsonObject.get("a.b"));

        jsonObject = JSON.parseObject("{'a.b':'C2'}");
        Assert.assertNull(OftenTool.getObjectAttrString(jsonObject, "a.b"));

        OftenTool.setObjectAttr(jsonObject, "c.d", "B");
        Assert.assertEquals("B", OftenTool.getObjectAttrString(jsonObject, "c.d"));
        Assert.assertNull(jsonObject.get("c.d"));

    }
}
