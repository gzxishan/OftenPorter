package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置对象为{@linkplain Properties}类型,配置数据为{@linkplain DefaultConfigData}.
 *
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public class DefaultAnnotationConfigable implements IAnnotationConfigable<Properties>
{
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.:$#-]+)\\}");

    @Override
    public String getValue(IConfigData config, String value)
    {
        if (config != null)
        {
            Matcher matcher = PATTERN.matcher(value);
            if (matcher.find())
            {
                StringBuilder stringBuilder = new StringBuilder();
                int from = 0;
                do
                {
                    String key = matcher.group(1);
                    String rs = config.getString(key, value);
                    stringBuilder.append(value, from, matcher.start());
                    stringBuilder.append(rs);
                    from = matcher.end();
                } while (matcher.find());
                stringBuilder.append(value, from, value.length());
                value = stringBuilder.toString();
            }
        }
        return value.trim();
    }

    @Override
    public IConfigData getConfig(Properties config)
    {
        return new DefaultConfigData(config);
    }

    @Override
    public void isConfig(Object configObject) throws RuntimeException
    {
        if (configObject != null && !(configObject instanceof Properties))
        {
            throw new IllegalArgumentException(
                    "expected type[" + Properties.class.getName() + "],but[" + configObject.getClass().getName()+"]");
        }
    }
}
