package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 十进制int类型.支持以下形式的变量参数:
 * <pre>
 *     1.in{number1,number2,number3,...}:表示其中一个值为有效值
 *     2.-in表示对in的反面
 *     3.range(from,to)/range[from,to)/range(from,to]/range[from,to],其中from或to可选，表示取值范围
 *     4.-range表示range的反面
 * </pre>
 */
public class IntParser extends TypeParser
{

    private static final Pattern PATTERN_IN_OUTER = Pattern.compile("^(in|-in)\\{([\\s\\S]*)\\}$");
    private static final Pattern PATTERN_IN_INNER = Pattern.compile("(-?[\\d]+),?");

    private static final Pattern PATTERN_RANGE = Pattern
            .compile("^(range|-range)([(\\[])(-?[\\d]*),(-?[\\d]*)([)\\]])$");

    public static class VarConfigRangeDealt
    {
        boolean isNot;
        Integer from, to;
        boolean containsLeft, containsRight;
        ITypeParser typeParser;

        public VarConfigRangeDealt(ITypeParser typeParser, String varConfig)
        {
            this.typeParser = typeParser;
            Matcher matcher = PATTERN_RANGE.matcher(varConfig);
            if (!matcher.find())
            {
                throw new IllegalArgumentException("error var config:" + varConfig);
            }
            isNot = matcher.group(1).equals("-range");
            containsLeft = matcher.group(2).equals("[");
            String str = matcher.group(3);
            if (!str.equals(""))
            {
                from = Integer.parseInt(str);
            }
            str = matcher.group(4);
            if (!str.equals(""))
            {
                to = Integer.parseInt(str);
            }
            containsRight = matcher.group(5).equals("]");
        }

        public Object getValue(int value)
        {
            boolean isInRange = true;
            if (from != null)
            {
                if (containsLeft)
                {
                    isInRange = value >= from;
                } else
                {
                    isInRange = value > from;
                }
            }
            if (isInRange && to != null)
            {
                if (containsRight)
                {
                    isInRange = value <= to;
                } else
                {
                    isInRange = value < to;
                }
            }

            Object obj = value;

            if (isNot)
            {
                if (isInRange)
                {
                    obj = ParserUtil.failed(typeParser, "参数值出现在range里。");
                }
            } else
            {
                if (!isInRange)
                {
                    obj = ParserUtil.failed(typeParser, "参数值不在range里。");
                }
            }

            return obj;
        }
    }

    public static class VarConfigInDealt
    {
        boolean isNot;
        private int[] in;
        ITypeParser typeParser;

        public VarConfigInDealt(ITypeParser typeParser, String varConfig)
        {
            this.typeParser = typeParser;
            Matcher outMatcher = PATTERN_IN_OUTER.matcher(varConfig);
            if (!outMatcher.find())
            {
                throw new IllegalArgumentException("error var config:" + varConfig);
            }
            isNot = outMatcher.group(1).equals("-in");

            Matcher innerMatcher = PATTERN_IN_INNER.matcher(outMatcher.group(2));
            List<Integer> inList = new ArrayList<>();

            while (innerMatcher.find())
            {
                inList.add(Integer.parseInt(innerMatcher.group(1)));
            }

            this.in = new int[inList.size()];
            for (int i = 0; i < this.in.length; i++)
            {
                this.in[i] = inList.get(i);
            }
        }

        public Object getValue(int value)
        {
            int index = -1;
            for (int i = 0; i < in.length; i++)
            {
                if (in[i] == value)
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
            }
            return value;

        }
    }

    @Override
    public ParseResult parse(WObject wObject, @NotNull String name, @NotNull Object value, @MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Integer)
            {
                v = value;
            } else
            {
                v = Integer.parseInt(value.toString());
            }

            if (dealt != null)
            {
                if (dealt instanceof VarConfigInDealt)
                {
                    VarConfigInDealt varConfigInDealt = (VarConfigInDealt) dealt;
                    v = varConfigInDealt.getValue((Integer) v);
                } else if (dealt instanceof VarConfigRangeDealt)
                {
                    VarConfigRangeDealt varConfigRangeDealt = (VarConfigRangeDealt) dealt;
                    v = varConfigRangeDealt.getValue((Integer) v);
                }
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this, e.getMessage());
        }
        return result;
    }


    @Override
    public Object initFor(ITypeParserOption parserOption)
    {
        String varConfig = parserOption.getNameConfig();
        Object dealt = null;
        if (varConfig.startsWith("-in") || varConfig.startsWith("in"))
        {
            dealt = new VarConfigInDealt(this, varConfig);
        } else
        {
            dealt = new VarConfigRangeDealt(this, varConfig);
        }

        return dealt;
    }
}
