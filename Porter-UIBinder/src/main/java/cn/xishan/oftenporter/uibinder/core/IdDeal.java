package cn.xishan.oftenporter.uibinder.core;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/2.
 */
public interface IdDeal
{
    final class Result
    {
        private boolean isOccur;
        private PortMethod method;
        private String[] funNames;
        private String varName;
        /**
         * 接口前缀
         */
        private String pathPrefix;

        @Override
        public String toString()
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("pathPrefix=").append(pathPrefix).append(",");
            if (isOccur())
            {
                stringBuilder.append("occur=true,funs:");
                for (int i = 0; i < funNames.length; i++)
                {
                    stringBuilder.append(funNames[i]).append(",");
                }
                stringBuilder.append("method=").append(method.name());
            } else
            {
                stringBuilder.append("occur=false,varName=").append(varName).append(",funs:");
                for (int i = 0; i < funNames.length; i++)
                {
                    stringBuilder.append(funNames[i]).append(",");
                }
            }
            return stringBuilder.toString();
        }

        public void setFunNames(String[] funNames)
        {
            this.funNames = funNames;
        }

        public void setVarName(String varName)
        {
            this.varName = varName;
        }

        public void setIsOccur(boolean isOccur)
        {
            this.isOccur = isOccur;
        }

        public void setPathPrefix(String pathPrefix)
        {
            this.pathPrefix = pathPrefix;
        }

        public String getPathPrefix()
        {
            return pathPrefix;
        }

        public void setMethod(PortMethod method)
        {
            this.method = method;
        }

        public String getVarName()
        {
            return varName;
        }

        public String[] getFunNames()
        {
            return funNames;
        }

        public PortMethod getMethod()
        {
            return method;
        }

        /**
         * 是否为触发控件。
         * @return
         */
        public boolean isOccur()
        {
            return isOccur;
        }
    }

    Result dealId(UiId id, String pathPrefix);
}
