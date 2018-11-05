package cn.xishan.oftenporter.porter.core.advanced;


import cn.xishan.oftenporter.porter.core.base.ParamSource;

/**
 * 用于地址处理。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface UrlDecoder
{
    public static class TiedValue
    {
        public String classTied;
        public String funTied;

        public TiedValue(String classTied, String funTied)
        {
            if (classTied != null)
            {
                PortUtil.checkName(classTied);
            }
            if (funTied != null)
            {
                PortUtil.checkName(funTied);
            }
            this.classTied = classTied;
            this.funTied = funTied;
        }
    }

    interface Result extends ParamSource
    {
        String contextName();

        String classTied();

        String funTied();

        void push(TiedValue tiedValue);

        TiedValue pop();

        /**
         * 框架内部处理参数时，获取参数值，优先从地址参数获取。
         *
         * @param name  设置的参数名称
         * @param value 参数值
         */
        void setParam(String name, Object value);
    }

    Result decode(String path)throws Exception;
}
