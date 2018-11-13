package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.bridge.BridgeUrlDecoder;

/**
 * 格式“:bridgeName/path”
 *
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class DefaultBridgeUrlDecoder implements BridgeUrlDecoder
{

    static class ResultImpl implements Result
    {
        private String bridgeName, path;

        public ResultImpl(String bridgeName, String path)
        {
            this.bridgeName = bridgeName;
            this.path = path;
        }

        @Override
        public String bridgeName()
        {
            return bridgeName;
        }

        @Override
        public String path()
        {
            return path;
        }
    }

    @Override
    public Result decode(String fullPath)
    {
        Result result = null;

        if (fullPath.startsWith(":"))
        {
            int index = fullPath.indexOf('/', 1);
            if (index != -1)
            {
                result = new ResultImpl(fullPath.substring(1, index), fullPath.substring(index));
            }
        }
        return result;
    }
}
