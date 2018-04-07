package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

/**
 * 状态监听接口。
 * <p>
 * 假如按顺序添加了如下监听器:S1,S2,...,Sx，则调用顺序如下：
 * <ol>
 * <li>顺序调用[S1,S2,...,Sx]:
 * {@linkplain #beforeSeek(InitParamSource, PorterConf, ParamSourceHandleManager) beforeSeek},
 * {@linkplain #afterSeek(InitParamSource, ParamSourceHandleManager) afterSeek}
 * </li>
 * <li>逆序调用[Sx,...,S2,S1]:
 * {@linkplain #afterStart(InitParamSource) afterStart}
 * </li>
 * <li>顺序调用[S1,S2,...,Sx]:
 * {@linkplain #beforeDestroy() beforeDestroy}
 * </li>
 * <li>逆序调用[Sx,...,S2,S1]:
 * {@linkplain #afterDestroy() afterDestroy}
 * </li>
 * </ol>
 * </p>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface StateListener
{
    void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
            ParamSourceHandleManager paramSourceHandleManager);

    void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager);

    void afterStart(InitParamSource initParamSource);

    void beforeDestroy();

    void afterDestroy();


    /**
     * @author Created by https://github.com/CLovinr on 2016/10/3.
     */
    public class Adapter implements StateListener
    {

        private final Logger LOGGER = LogUtil.logger(Adapter.class);

        @Override
        public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
                ParamSourceHandleManager paramSourceHandleManager)
        {
            LOGGER.debug("beforeSeek");
        }

        @Override
        public void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager)
        {
            LOGGER.debug("afterSeek");
        }

        @Override
        public void afterStart(InitParamSource initParamSource)
        {
            LOGGER.debug("afterStart");
        }

        @Override
        public void beforeDestroy()
        {
            LOGGER.debug("beforeDestroy");
        }

        @Override
        public void afterDestroy()
        {
            LOGGER.debug("afterDestroy");
        }
    }

}
