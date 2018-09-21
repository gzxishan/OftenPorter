package cn.xishan.oftenporter.demo.oftendb.test1;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */
public class TestForMem2
{
    static class TempObject{
        Object object = new Object();

        public TempObject()
        {
            System.out.println(this.hashCode());
        }
    }

    public static void main(String[] args) throws InterruptedException
    {


        while (true){
            test();
            Thread.sleep(500);
        }

    }

    static void test(){
        for (int i = 0; i < 1000; i++)
        {
            new TempObject();
        }
    }
}
