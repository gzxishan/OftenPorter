package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <pre>
 *     支持以下形式的变量参数:
 *     1.in{'str1'[:'value1'],'str2[:value2]',...},表示其中一个值为有效值,其中单引号“'”和[:value]可选，表示最终结果会被转换成该值.
 *     2.-in表示in反面,其中[:value]将无效.
 *     3.[]表示数组，会转换成string数组;array表示json数组
 *     4.json表示json对象
 *     5.date表示"yyyy-MM-dd"格式的日期
 *     6.date-minute表示"yyyy-MM-dd HH:mm"格式的日期
 *     7.date-time表示"yyyy-MM-dd HH:mm:ss""格式的日期
 *     8.date-month表示"yyyy-MM"格式的日期
 *     9.$reg:xxxx表示正则表达式
 *     10.bool[:defaultValue]表示boolean值，且当为false、0时、结果为false,当为空时为defaultValue（默认为false），其余为true
 *     11.int与long分别表示为int与long型
 *     12.float与double分别表示为float与double型
 *     13.括号支持转义字符：如"varName(\\(\\))[\\[\\]]"
 * </pre>
 * Created by 宇宙之灵 on 2015/9/14.
 */
public class StringParser extends TypeParser<StringParser.StringDealt>
{

    private static final Pattern PATTERN_OUTER = Pattern.compile("^(in|-in)\\{([\\s\\S]*)\\}$");
    private static final Pattern PATTERN_INNER = Pattern.compile("(('[^']*')|([^',]*))(:(('[^']*')|([^',]*)))?,?");

    static interface StringDealt
    {
        ParseResult getValue(ITypeParser iTypeParser, Object value);
    }

    static interface StringEmptyableDealt extends StringDealt
    {
        ParseResult getValueForEmpty(ITypeParser iTypeParser);
    }

    public static class VarConfigDealt implements StringDealt
    {
        boolean isNot;
        private String[] in;
        private String[] to;

        public VarConfigDealt(String varConfig)
        {
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

            this.in = inList.toArray(OftenTool.EMPTY_STRING_ARRAY);
            this.to = toList.toArray(OftenTool.EMPTY_STRING_ARRAY);
        }

        @Override
        public ParseResult getValue(ITypeParser iTypeParser, Object value)
        {
            value = String.valueOf(value);
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
                    return ParserUtil.failed(iTypeParser, "参数值出现在-in{}列表里。");
                }
            } else
            {
                if (index == -1)
                {
                    return ParserUtil.failed(iTypeParser, "参数值不在in{}列表里。");
                }
                if (to[index] != null)
                {
                    value = to[index];
                }
            }
            return new ParseResult(value);
        }
    }

    @Override
    public ParseResult parse(OftenObject oftenObject,@NotNull String name, @NotNull Object value, StringDealt stringDealt)
    {
        ParseResult parseResult;
        if (stringDealt != null)
        {
            parseResult = stringDealt.getValue(this, value);
        } else
        {
            parseResult = new ParseResult(String.valueOf(value));
        }
        return parseResult;
    }

    @Override
    public ParseResult parseEmpty(OftenObject oftenObject, String name, StringDealt dealt)
    {
        if (dealt != null && dealt instanceof StringEmptyableDealt)
        {
            StringEmptyableDealt stringEmptyableDealt = (StringEmptyableDealt) dealt;
            return stringEmptyableDealt.getValueForEmpty(this);
        }
        return null;
    }

    @Override
    public StringDealt initFor(ITypeParserOption parserOption)
    {
        StringDealt stringDealt;
        String config = parserOption.getNameConfig();
        if (config.equals("[]"))
        {
            stringDealt = StringArrayParser::parseArray;
        } else if (config.equals("array"))
        {
            stringDealt = JSONArrayParser::parse;
        } else if (config.equals("json"))
        {
            stringDealt = JSONObjectParser::parse;
        } else if (config.equals("date"))
        {
            stringDealt = newDateDealt("yyyy-MM-dd");
        } else if (config.equals("date-time"))
        {
            stringDealt = newDateDealt("yyyy-MM-dd HH:mm:ss");
        } else if (config.equals("date-month"))
        {
            stringDealt = newDateDealt("yyyy-MM");
        } else if (config.equals("date-minute"))
        {
            stringDealt = newDateDealt("yyyy-MM-dd HH:mm");
        } else if (config.equals("int"))
        {
            stringDealt = (iTypeParser, value) -> {
                ParseResult result;
                try
                {
                    if (!(value instanceof Integer))
                    {
                        value = Integer.parseInt(String.valueOf(value));
                    }
                    result = new ParseResult(value);
                } catch (Exception e)
                {
                    result = ParserUtil.failed(iTypeParser, e.getMessage());
                }
                return result;
            };
        } else if (config.equals("long"))
        {
            stringDealt = (iTypeParser, value) -> {
                ParseResult result;
                try
                {
                    if (!(value instanceof Long))
                    {
                        value = Long.parseLong(String.valueOf(value));
                    }
                    result = new ParseResult(value);
                } catch (Exception e)
                {
                    result = ParserUtil.failed(iTypeParser, e.getMessage());
                }
                return result;
            };
        } else if (config.equals("float"))
        {
            stringDealt = (iTypeParser, value) -> {
                ParseResult result;
                try
                {
                    if (!(value instanceof Float))
                    {
                        value = Float.parseFloat(String.valueOf(value));
                    }
                    result = new ParseResult(value);
                } catch (Exception e)
                {
                    result = ParserUtil.failed(iTypeParser, e.getMessage());
                }
                return result;
            };
        } else if (config.equals("double"))
        {
            stringDealt = (iTypeParser, value) -> {
                ParseResult result;
                try
                {
                    if (!(value instanceof Double))
                    {
                        value = Double.parseDouble(String.valueOf(value));
                    }
                    result = new ParseResult(value);
                } catch (Exception e)
                {
                    result = ParserUtil.failed(iTypeParser, e.getMessage());
                }
                return result;
            };
        } else if (config.startsWith("bool"))
        {
            int index = config.indexOf(":");
            final boolean defaultValue = index != -1 && Boolean.parseBoolean(config.substring(index + 1));
            stringDealt = new StringEmptyableDealt()
            {
                @Override
                public ParseResult getValueForEmpty(ITypeParser iTypeParser)
                {
                    return new ParseResult(defaultValue);
                }

                @Override
                public ParseResult getValue(ITypeParser iTypeParser, Object value)
                {
                    ParseResult result;
                    try
                    {
                        if (!(value instanceof Boolean))
                        {
                            value = !(value.equals("false") || value.equals("0"));
                        }
                        result = new ParseResult(value);
                    } catch (Exception e)
                    {
                        result = ParserUtil.failed(iTypeParser, e.getMessage());
                    }
                    return result;
                }
            };
        } else if (config.startsWith("$reg:"))
        {
            stringDealt = new StringDealt()
            {
                Pattern pattern = Pattern.compile(config.substring(5));

                @Override
                public ParseResult getValue(ITypeParser iTypeParser, Object value)
                {
                    ParseResult result;
                    try
                    {
                        value = String.valueOf(value);
                        Matcher matcher = pattern.matcher((String) value);
                        if (matcher.find())
                        {
                            result = new ParseResult(value);
                        } else
                        {
                            result = ParserUtil.failed(iTypeParser, "illegal value:" + value);
                        }
                    } catch (Exception e)
                    {
                        result = ParserUtil.failed(iTypeParser, e.getMessage());
                    }
                    return result;
                }
            };
        } else
        {
            stringDealt = new VarConfigDealt(parserOption.getNameConfig());
        }
        return stringDealt;
    }

    static StringDealt newDateDealt(String format)
    {
        StringDealt stringDealt = (iTypeParser, value) -> {
            ParseResult result;
            try
            {
                Object v;
                if (value instanceof Date)
                {
                    v = value;
                } else if (value instanceof Long)
                {
                    v = new Date((long) value);
                } else
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    v = simpleDateFormat.parse(String.valueOf(value));
                }
                result = new ParseResult(v);
            } catch (Exception e)
            {
                result = ParserUtil.failed(iTypeParser, e.getMessage());
            }
            return result;
        };
        return stringDealt;
    }
}
