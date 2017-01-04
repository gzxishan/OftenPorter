package cn.xishan.oftenporter.uibinder.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by 刚帅 on 2015/11/22.
 */
public class FireBlock
{

    boolean isExcept;
    private Map<String, Set<String>> map = new HashMap<>();

    /**
     * @param isExcept 是否是排除
     */
    public FireBlock(boolean isExcept)
    {
        this.isExcept = isExcept;
    }

    public FireBlock add(String pathPrefix, String... tiedFuns)
    {
        Set<String> set = map.get(pathPrefix);
        if (set == null)
        {
            set = new HashSet<>();
            map.put(pathPrefix, set);
        }
        for (int i = 0; i < tiedFuns.length; i++)
        {
            set.add(tiedFuns[i]);
        }

        return this;
    }

    /**
     * 判断是否会被触发
     * @param pathPrefix
     * @param tiedFun
     * @return
     */
    public boolean willFire(String pathPrefix, String tiedFun)
    {
        Set<String> set = map.get(pathPrefix);
        if (isExcept)
        {
            return set == null || !set.contains(tiedFun);
        } else
        {
            return set != null && set.contains(tiedFun);
        }

    }
}
