package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.servlet.ContentType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-12-09.
 */
public class HttpUtilTest
{
    //@Test
    public void testRequestString(){
        try
        {
            RequestData requestData=new RequestData();
            requestData.setContentType(ContentType.TEXT_PLAIN);
            requestData.setEncoding("utf-8");
            String content = HttpUtil.requestString(requestData, PortMethod.GET,null,"https://www.baidu.com");
            System.out.println(content);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //@Test
    public void testRequestBytes(){
        try
        {
            RequestData requestData=new RequestData();
            requestData.setContentType(ContentType.TEXT_PLAIN);
            requestData.setEncoding("utf-8");
            byte[] bs = HttpUtil.requestBytes(requestData, PortMethod.GET,null,"https://www.baidu.com");
            System.out.println(new String(bs));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}