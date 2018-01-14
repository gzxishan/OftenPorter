package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <pre>
 *     支持以下形式的变量参数:
 *     1.in{'str1'[:'value1'],'str2[:value2]',...},表示其中一个值为有效值,其中单引号“'”和[:value]可选，表示最终结果会被转换成该值.
 *     2.-in表示in反面,其中[:value]将无效.
 * </pre>
 * Created by 宇宙之灵 on 2015/9/14.
 */
public class StringParser extends TypeParser<StringParser.VarConfigDealt>
{

    private static final Pattern PATTERN_OUTER = Pattern.compile("^(in|-in)\\{([\\s\\S]*)\\}$");
    private static final Pattern PATTERN_INNER = Pattern.compile("(('[^']*')|([^',]*))(:(('[^']*')|([^',]*)))?,?");

    public static class VarConfigDealt
    {
        boolean isNot;
        private String[] in;
        private String[] to;
        ITypeParser typeParser;

        public VarConfigDealt(ITypeParser typeParser, String varConfig)
        {
            this.typeParser = typeParser;
            Matcher outMatcher = PATTERN_OUTER.matcher(varConfig);
            if (!outMatcher.find())
            {
                throw new IllegalArgumentException("error var config:" + varConfig);
            }
            isNot = outMatcher.group(1).equals("-in");

            Matcher innerMatcher = PATTERN_INNER.matcher(outMatcher.group(2));
            List<String> inList = new ArrayList<>();
            List<String> toList = new ArrayList<>();

            while (innerMatcher.find())
            {
                String in = innerMatcher.group(1);
                if (in.startsWith("'"))
                {
                    in = in.substring(1, in.length() - 1);
                }
                String to = innerMatcher.group(4);
                if (to != null)
                {
                    to = to.substring(1);
                    if (to.startsWith("'"))
                    {
                        to = to.substring(1, to.length() - 1);
                    }
                }
                inList.add(in);
                toList.add(to);
            }

            this.in = inList.toArray(new String[0]);
            this.to = toList.toArray(new String[0]);
        }

        public Object getValue(String value)
        {
            int index = -1;
            for (int i = 0; i < in.length; i++)
            {
                if (in[i].equals(value))
                {
                    index = i;
                    break;
                }
            }
            if (isNot)
            {
                if (index >= 0)
                {
                    return ParserUtil.failed(typeParser, "参数值出现在-in{}列表里。");
                }
            } else
            {
                if (index == -1)
                {
                    return ParserUtil.failed(typeParser, "参数值不在in{}列表里。");
                }
                if (to[index] != null)
                {
                    value = to[index];
                }
            }
            return value;

        }
    }

    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, VarConfigDealt varConfigDealt)
    {
        value = String.valueOf(value);
        if(varConfigDealt!=null){
            value = varConfigDealt.getValue((String) value);
        }
        return new ParseResult(value);
    }


    @Override
    public VarConfigDealt initFor(ITypeParserOption parserOption)
    {
        VarConfigDealt varConfigDealt;
        varConfigDealt = new VarConfigDealt(this, parserOption.getNameConfig());

        return varConfigDealt;
    }
}
