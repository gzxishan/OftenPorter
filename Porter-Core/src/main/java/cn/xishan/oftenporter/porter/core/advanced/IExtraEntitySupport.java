package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 另见{@linkplain Porter}、{@linkplain PorterOfFun}
 *
 * @author Created by https://github.com/CLovinr on 2018-09-04.
 */
public interface IExtraEntitySupport
{
    Class<?> putExtraEntity(String key, Class<?> entityClass);

    Class<?> removeExtraEntity(String key);

    Class<?> getExtraEntity(String key);

    Set<String> getExtralKeySet();


    class ExtraEntitySupportImpl implements IExtraEntitySupport
    {
        private Map<String, Class> map = new HashMap<>(1);

        @Override
        public Class<?> putExtraEntity(String key, Class<?> entityClass)
        {
            return map.put(key, entityClass);
        }

        @Override
        public Class<?> removeExtraEntity(String key)
        {
            return map.remove(key);
        }

        @Override
        public Class<?> getExtraEntity(String key)
        {
            return map.get(key);
        }

        @Override
        public Set<String> getExtralKeySet()
        {
            return map.keySet();
        }
    }
}
