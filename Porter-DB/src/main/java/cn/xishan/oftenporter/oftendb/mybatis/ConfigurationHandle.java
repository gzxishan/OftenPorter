package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.exception.InitException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2018-03-16.
 */
class ConfigurationHandle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHandle.class);



    public static void setForOverride(Configuration configuration){
        try
        {
            Field field = configuration.getClass().getDeclaredField("mappedStatements");
            field.setAccessible(true);
            Map<String, MappedStatement> mappedStatements = (Map<String, MappedStatement>) field.get(configuration);
            MyMap myMap = new MyMap(mappedStatements);
            field.set(configuration,myMap);
        } catch (Exception e)
        {
            throw new InitException(e);
        }
    }




    static class MyMap implements Map<String, MappedStatement>, Serializable
    {

        private static final long serialVersionUID = 8355736311503372043L;
        private final Map<String, MappedStatement> mappedStatements;

        public MyMap(Map<String, MappedStatement> mappedStatements)
        {
            this.mappedStatements = mappedStatements;
        }

        @Override
        public int size()
        {
            return mappedStatements.size();
        }

        @Override
        public boolean isEmpty()
        {
            return mappedStatements.isEmpty();
        }

        @Override
        public boolean containsKey(Object key)
        {
            return mappedStatements.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value)
        {
            return mappedStatements.containsValue(value);
        }

        @Override
        public MappedStatement get(Object key)
        {
            return mappedStatements.get(key);
        }

        @Override
        public MappedStatement put(String key, MappedStatement value)
        {
            if (mappedStatements.containsKey(key))
            {
                LOGGER.warn("Mapped Statements collection override: {}", key);
                mappedStatements.remove(key);
            }
            return mappedStatements.put(key, value);
        }

        @Override
        public MappedStatement remove(Object key)
        {
            return mappedStatements.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ? extends MappedStatement> m)
        {
            mappedStatements.putAll(m);
        }

        @Override
        public void clear()
        {
            mappedStatements.clear();
        }

        @Override
        public Set<String> keySet()
        {
            return mappedStatements.keySet();
        }

        @Override
        public Collection<MappedStatement> values()
        {
            return mappedStatements.values();
        }

        @Override
        public Set<Entry<String, MappedStatement>> entrySet()
        {
            return mappedStatements.entrySet();
        }

        @Override
        public boolean equals(Object o)
        {
            return mappedStatements.equals(o);
        }

        @Override
        public int hashCode()
        {
            return mappedStatements.hashCode();
        }
    }

}
