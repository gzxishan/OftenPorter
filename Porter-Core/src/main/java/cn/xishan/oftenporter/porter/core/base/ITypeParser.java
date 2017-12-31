package cn.xishan.oftenporter.porter.core.base;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型转换。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ITypeParser
{

    /**
     * @param name         参数名
     * @param value        参数值
     * @param parserOption 额外选项
     * @return
     */
    ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption);

    /**
     * 初始化框架时会调用，不保证对所有的都进行调用。
     *
     * @param parserOption
     */
    <T> T dealtFor(@NotNull ITypeParserOption parserOption);

    /**
     * 每一个TypeParser都会被放到一个全局的Store中，此id就是唯一对应的键值。
     *
     * @return 全局唯一的id。
     */
    String id();


    /**
     * 用于分解参数。
     * <pre>
     *     假如：dec=obj
     *     obj为分解类型：1）若obj中含有名为dec的值val，则最终dec=val；。
     * </pre>
     * 返回的值会被统一放到一个临时Map中，以后在获取参数值时，优先从该map中获取,并且从它中获取的参数无法再被转换。
     */
    public class DecodeParams
    {

        private Map<String, Object> params = new HashMap<>();

        public DecodeParams()
        {

        }

        public Map<String, Object> getParams()
        {
            return params;
        }

        public DecodeParams put(String name, Object value)
        {
            params.put(name, value);
            return this;
        }
    }

    public class ParseResult
    {


        private boolean isLegal;
        private Object value;
        private String failedDesc;

        /**
         * 转换不合法
         */
        public ParseResult()
        {
            this.isLegal = false;
        }

        /**
         * 转换合法
         *
         * @param value 结果值,可以为{@link ITypeParser.DecodeParams}类型。
         */
        public ParseResult(@NotNull Object value)
        {
            setValue(value);
            this.isLegal = true;
        }


        public static ParseResult failed(String failedDesc)
        {
            ParseResult parseResult = new ParseResult();
            parseResult.setFailedDesc(failedDesc);
            return parseResult;
        }

        public void setFailedDesc(String failedDesc)
        {
            setIsLegal(false);
            this.failedDesc = failedDesc;
        }

        public String getFailedDesc()
        {
            return failedDesc;
        }

        /**
         * 设置值
         *
         * @param value
         */
        public void setValue(@NotNull Object value)
        {
            if (value == null)
            {
                throw new NullPointerException();
            }
            this.value = value;
        }

        /**
         * 设置结果是否合法
         *
         * @param isLegal
         */
        private void setIsLegal(boolean isLegal)
        {
            this.isLegal = isLegal;
        }

        public boolean isLegal()
        {
            return isLegal;
        }

        /**
         * 得到转换后的值
         *
         * @return
         */
        public Object getValue()
        {
            return value;
        }
    }
}
