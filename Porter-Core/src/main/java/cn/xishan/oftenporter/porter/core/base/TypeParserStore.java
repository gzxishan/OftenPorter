package cn.xishan.oftenporter.porter.core.base;

/**
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public interface TypeParserStore
{
    ITypeParser byId(String id);

    /**
     * 获得默认的。
     * @return
     */
    String getDefaultTypeParserId();

    void putParser(ITypeParser typeParser);

    boolean contains(ITypeParser typeParser);
}
