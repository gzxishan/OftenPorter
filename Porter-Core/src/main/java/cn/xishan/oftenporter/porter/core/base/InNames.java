package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.advanced.BackableSeek;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        private String defaultValue;
        private Class type;
        private _Nece nece;
        private _Nece[] typeNeces;
        private Field[] typeNeceFields;

        private ITypeParserOption parserOption;
        private Object dealt;

        static final Pattern VAR_NAME_PATTERN = Pattern.compile("^([a-zA-Z0-9%_.$\\-]+)");
        static final Pattern VAR_NAME_CONF_PATTERN = Pattern.compile("^\\(([^()]*)\\)");
        static final Pattern VAR_NAME_DEFAULT_PATTERN = Pattern.compile("^\\[([^\\[\\]]*)\\]");

        public Name(String varNameWithConfig, BackableSeek backableSeek)
        {
            this(varNameWithConfig);
            this.typeParserId = backableSeek.getTypeId(varName);
        }

        public Name(String varName, String typeId)
        {
            this(varName);
            this.typeParserId = typeId;
        }

        public Name(String str)
        {
            str = str.trim();
            Matcher matcher = VAR_NAME_PATTERN.matcher(str);
            if (!matcher.find())
            {
                throw new RuntimeException("illegal var name");
            }
            this.varName = matcher.group(1);
            str = str.substring(matcher.end()).trim();

            if (str.startsWith("("))
            {
                str = findConf(str);
                str = findDefault(str);
            } else
            {
                str = findDefault(str);
                str = findConf(str);
            }
        }

        private String findConf(String str)
        {
            Matcher matcher = VAR_NAME_CONF_PATTERN.matcher(str);
            if (matcher.find())
            {
                String varConfig = matcher.group(1).trim();
                if (varConfig.equals(""))
                {
                    this.parserOption = EMPTY_TYPE_PARSER_OPTION;
                } else
                {
                    this.parserOption = () -> varConfig;
                }
                str = str.substring(matcher.end()).trim();
            } else
            {
                this.parserOption = NULL_TYPE_PARSER_OPTION;
            }
            return str;
        }

        private String findDefault(String str)
        {
            Matcher matcher = VAR_NAME_DEFAULT_PATTERN.matcher(str);
            if (matcher.find())
            {
                this.defaultValue = matcher.group(1).trim();
                str = str.substring(matcher.end()).trim();
            }
            return str;
        }


        public static String removeConfig(String varName)
        {
            Matcher matcher = VAR_NAME_PATTERN.matcher(varName);
            if (!matcher.find())
            {
                throw new RuntimeException("illegal var name");
            }
            varName = matcher.group(1);
            return varName;
        }

        public Class getType()
        {
            return type;
        }

        public Object getTypeObject(OftenObject oftenObject, ParamSource paramSource) throws Exception
        {
            Object v = null;
            if (type != null && !PortUtil.willIgnoreAdvanced(type) && !OftenTool.isAssignable(type, CharSequence.class))
            {
                v = paramSource.getParam(type.getName());//
                if (v != null && OftenTool.notNullAndEmpty(typeNeces))
                {
                    for (int i = 0; i < this.typeNeces.length; i++)
                    {
                        if (this.typeNeces[i].isNece(oftenObject) && OftenTool.isEmpty(this.typeNeceFields[i].get(v)))
                        {
                            String typeVarName = this.typeNeces[i].getVarName();
                            v = DefaultFailedReason
                                    .lackNecessaryParams("Lack necessary params for " + varName + "." + typeVarName,
                                            typeVarName);
                            break;
                        }
                    }
                }
            }
            return v;
        }

        public void setType(AnnotationDealt annotationDealt, Class type, _Nece nece)
        {
            this.type = type;
            this.nece = nece;
            if (nece == null && type != null && !PortUtil.willIgnoreAdvanced(type))
            {
                Field[] fieds = OftenTool.getAllFields(type);
                List<_Nece> neceList = new ArrayList<>();
                List<Field> fieldList = new ArrayList<>();
                for (Field f : fieds)
                {
                    _Nece fNece = annotationDealt.nece(f);
                    if (fNece != null)
                    {
                        neceList.add(fNece);
                        f.setAccessible(true);
                        fieldList.add(f);
                    }
                }
                this.typeNeces = neceList.toArray(new _Nece[0]);
                this.typeNeceFields = fieldList.toArray(new Field[0]);
            } else
            {
                this.typeNeces = null;
                this.typeNeceFields = null;
            }
        }

        public _Nece getNece()
        {
            return nece;
        }


        public String getDefaultValue()
        {
            return defaultValue;
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

    //public final _Nece[] neceDeals;

    /**
     * 非必需参数的名称
     */
    public final Name[] unece;
    public final Name[] inner;


    public InNames(Name[] nece, Name[] unece, Name[] inner)
    {
        this.nece = nece;
        //this.neceDeals = neceDeals == null || neceDeals.length == 0 ? null : neceDeals;
        this.unece = unece;
        this.inner = inner;
    }


    private static final Name[] EMPTY = new Name[0];


    public static InNames temp(Name name)
    {
        return new InNames(new Name[]{name}, EMPTY, EMPTY);
    }


    public static InNames fromStringArray(String[] nece, String[] unece, String[] inner)
    {
        return new InNames(toNames(nece), toNames(unece), toNames(inner));
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
            names[i] = new Name(strs[i]);
        }
        return names;
    }
}
