package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018-12-21.
 */
public class DealSharpProperties
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DealSharpProperties.class);

    private static class PropOne
    {
        private String propKey,
                originValue;

        private int startIndex, endIndex;

        public PropOne(String propKey, String originValue, int startIndex, int endIndex)
        {
            this.propKey = propKey;
            this.originValue = originValue;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getPropKey()
        {
            return propKey;
        }

        public String replace(String propValue)
        {
            String str = originValue.substring(0, startIndex) + propValue + originValue.substring(endIndex);
            return str;
        }
    }

    /**
     * 替换#{properName}变量。
     *
     * @param srcMap        待替换属性值的map
     * @param propertiesMap 提供属性的map
     */
    public static void dealSharpProperties(Map srcMap, Map propertiesMap)
    {
        Set<String> containsVar = null;
        boolean isFirst = true;
        boolean hasSet = true;
        //处理properties
        while (hasSet)
        {
            hasSet = false;
            Collection<String> nameCollection;
            if (isFirst)
            {
                nameCollection = srcMap.keySet();
            } else
            {
                nameCollection = containsVar;
            }
            containsVar = new HashSet<>();
            for (String properName : nameCollection)
            {
                Object value = srcMap.get(properName);
                if (!(value instanceof CharSequence))
                {
                    continue;
                }
                String valueString = String.valueOf(value);

                PropOne propOne = getPropertiesKey(String.valueOf(valueString));
                if (propOne != null && propOne.getPropKey().equals(properName))
                {
                    throw new RuntimeException(
                            "can not set property of " + properName + " with value \"" + valueString + "\"");
                } else if (propOne != null && propertiesMap.containsKey(propOne.getPropKey()))
                {
                    containsVar.add(properName);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("replace sharp property:key={},replace-attr={},origin-value={}", properName,
                                propOne.getPropKey(), valueString);
                    }
                    String newValue = propOne.replace(String.valueOf(propertiesMap.get(propOne.getPropKey())));
                    srcMap.put(properName, newValue);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("replace sharp property:key={},new-value={}", properName, newValue);
                    }
                    hasSet = true;
                }
            }
            isFirst = false;
        }
    }

    static void dealProperties(IConfigData configData)
    {
        Set<String> containsVar = null;
        boolean isFirst = true;
        boolean hasSet = true;
        //处理properties
        while (hasSet)
        {
            hasSet = false;
            Collection<String> nameCollection;
            if (isFirst)
            {
                nameCollection = configData.propertyNames();
            } else
            {
                nameCollection = containsVar;
            }
            containsVar = new HashSet<>();
            for (String properName : nameCollection)
            {
                Object value = configData.get(properName);
                if (!(value instanceof CharSequence))
                {
                    continue;
                }
                String valueString = String.valueOf(value);

                PropOne propOne = getPropertiesKey(String.valueOf(valueString));
                if (propOne != null && propOne.getPropKey().equals(properName))
                {
                    throw new RuntimeException(
                            "can not set property of " + properName + " with value \"" + valueString + "\"");
                } else if (propOne != null && configData.contains(propOne.getPropKey()))
                {
                    containsVar.add(properName);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("replace sharp property:key={},replace-attr={},origin-value={}", properName,
                                propOne.getPropKey(), valueString);
                    }
                    String newValue = propOne.replace(configData.getString(propOne.getPropKey()));
                    configData.set(properName, newValue);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("replace sharp property:key={},new-value={}", properName, newValue);
                    }
                    hasSet = true;
                }
            }
            isFirst = false;
        }
    }

    private static final Pattern PROPERTIES_PATTERN = Pattern.compile("#\\{([^{}]+)}");

    private static PropOne getPropertiesKey(String value)
    {
        Matcher matcher = PROPERTIES_PATTERN.matcher(value);
        if (matcher.find())
        {
            PropOne propOne = new PropOne(matcher.group(1).trim(), value, matcher.start(), matcher.end());
            return propOne;
        } else
        {
            return null;
        }
    }

}
