package cn.xishan.oftenporter.oftendb.mybatis;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2018-03-16.
 */
class ConfigurationEnableIdOverride extends Configuration
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationEnableIdOverride.class);

    public ConfigurationEnableIdOverride(Environment environment)
    {
        super(environment);
    }

    @Override
    public void addMappedStatement(MappedStatement ms)
    {
        String key = ms.getId();
        if (mappedStatements.containsKey(key))
        {
            LOGGER.warn("Mapped Statements collection override: {}", key);
            mappedStatements.remove(key);
        }
        super.addMappedStatement(ms);
    }
}
