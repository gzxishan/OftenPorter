package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;

/**
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public interface CommonMain {
    /**
     * 见{@linkplain AutoSet.Range#Global}
     *
     * @param name
     * @param object
     */
    void addGlobalAutoSet(String name, Object object);

    void addGlobalTypeParser(ITypeParser typeParser);


    /**
     * 添加针对所有context有效的全局检测。
     * <br>
     * <strong>注意：</strong>在未启动任何context时有效，否则会抛出异常。
     *
     * @param checkPassable
     */
    void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException;

    /**
     * 创建一个配置对象
     *
     * @return
     */
    PorterConf newPorterConf();

    /**
     * 开启一个Context。
     *
     * @param porterConf
     */
    void startOne(PorterConf porterConf);

    PLinker getPLinker();

    /**
     * 销毁指定的context。
     *
     * @param contextName
     */
    void destroyOne(String contextName);

    /**
     * 禁用或启用指定的Context
     *
     * @param contextName
     * @param enable
     */
    void enableOne(String contextName, boolean enable);

    /**
     * 销毁所有的context
     */
    void destroyAll();

}
