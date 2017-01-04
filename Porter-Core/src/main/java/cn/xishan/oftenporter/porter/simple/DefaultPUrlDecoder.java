package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.pbridge.PUrlDecoder;

/**
 * 格式“:pName/path”
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class DefaultPUrlDecoder implements PUrlDecoder
{

    static class ResultImpl implements Result
    {
        private String pName, path;

        public ResultImpl(String pName, String path)
        {
            this.pName = pName;
            this.path = path;
        }

        @Override
        public String pName()
        {
            return pName;
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
