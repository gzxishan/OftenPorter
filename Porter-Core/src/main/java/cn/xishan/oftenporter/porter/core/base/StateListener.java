package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

/**
 * 状态监听接口。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface StateListener
{
    void beforeSeek(InitParamSource initParamSource,PorterConf porterConf, ParamSourceHandleManager paramSourceHandleManager);

    void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager);

    void afterStart(InitParamSource initParamSource);

    void beforeDestroy();

    void afterDestroy();


    /**
     * @author Created by https://github.com/CLovinr on 2016/10/3.
     */
    public class Adapter implements StateListener
    {

        private  final Logger LOGGER = LogUtil.logger(Adapter.class);

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
