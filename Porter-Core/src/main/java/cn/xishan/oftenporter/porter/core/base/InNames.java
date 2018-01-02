package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.exception.InitException;

/**
 * 用于存储参数的名称
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public class InNames
{
    private static final ITypeParserOption NULL_TYPE_PARSER_OPTION = () -> null;
    private static final ITypeParserOption EMPTY_TYPE_PARSER_OPTION = () -> "";


    public static class Name
    {
        /**
         * 变量名,用于获取源参数
         */
        public final String varName;
        /**
         * 转换对应的id，用于获取{@linkplain ITypeParser}。
         */
        public String typeParserId;

        private ITypeParserOption parserOption;
        private Object dealt;

        public Name(String varName, String typeParserId)
        {
            int index1 = varName.indexOf('(');
            int index2 = varName.lastIndexOf(')');
            if (index1 != -1 || index2 != -1)
            {
                if (index1 == -1 || index2 == -1 || index1 > index2)
                {
                    throw new InitException("varname config error:" + varName);
                }
                String varConfig = varName.substring(index1 + 1, index2).trim();
                if (varConfig.equals(""))
                {
                    this.parserOption = EMPTY_TYPE_PARSER_OPTION;
                } else
                {
                    this.parserOption = () -> varConfig;
                }
                varName = varName.substring(0, index1);
            } else
            {
                this.parserOption = NULL_TYPE_PARSER_OPTION;
            }

            this.varName = varName;
            if (typeParserId != null)
            {
                this.typeParserId = typeParserId;
            }
        }

        public <D> D getDealt()
        {
            return (D) dealt;
        }

        public void doDealtFor(ITypeParser typeParser)
        {
            if (parserOption != NULL_TYPE_PARSER_OPTION && parserOption != EMPTY_TYPE_PARSER_OPTION)
            {
                dealt = typeParser.initFor(parserOption);
                parserOption = null;
            }
        }
    }

    /**
     * 必需参数的名称
     */
    public final Name[] nece;

    public final _Nece[] neceDeals;

    /**
     * 非必需参数的名称
     */
    public final Name[] unece;
    public final Name[] inner;

    public InNames(Name[] nece, Name[] unece, Name[] inner)
    {
        this(nece, null, unece, inner);
    }

    public InNames(Name[] nece, _Nece[] neceDeals, Name[] unece, Name[] inner)
    {
        this.nece = nece;
        this.neceDeals = neceDeals == null || neceDeals.length == 0 ? null : neceDeals;
        this.unece = unece;
        this.inner = inner;
    }


    private static final Name[] EMPTY = new Name[0];


    public static InNames temp(Name name)
    {
        return new InNames(new Name[]{name}, null, EMPTY, EMPTY);
    }

    public static InNames fromStringArray(String[] nece, String[] unece, String[] inner)
    {
        return new InNames(toNames(nece), null, toNames(unece), toNames(inner));
    }

    private static Name[] toNames(String[] strs)
    {
        if (strs == null)
        {
            return EMPTY;
        }
        Name[] names = new Name[strs.length];
        for (int i = 0; i < names.length; i++)
        {
            names[i] = new Name(strs[i], null);
        }
        return names;
    }
}
