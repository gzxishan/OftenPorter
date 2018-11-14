package cn.xishan.oftenporter.porter.local.porter2;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/13.
 */
@PortIn("My")
public class MyPorter
{
    private String words;

    public MyPorter(String words)
    {
        this.words = words;
    }



}
