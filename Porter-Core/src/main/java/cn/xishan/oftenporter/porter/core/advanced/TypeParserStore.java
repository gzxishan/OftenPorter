package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

/**
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public interface TypeParserStore
{
    ITypeParser byId(@MayNull String id);

    /**
     * 获得默认的。
     *
     * @return
     */
    String getDefaultTypeParserId();

    void putParser(ITypeParser typeParser);

    boolean contains(ITypeParser typeParser);
}
