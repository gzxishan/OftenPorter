package cn.xishan.oftenporter.bridge.http;


import cn.xishan.oftenporter.porter.core.JResponse;

/**
 * Created by 宇宙之灵 on 2016/7/3.
 */
public interface JRCallback
{
    void onResult(JResponse jResponse);

    public static final JRCallback EMPTY = new JRCallback()
    {
        @Override
        public void onResult(JResponse jResponse)
        {

        }
    };
}
