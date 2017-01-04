package cn.xishan.oftenporter.porter.core.base;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于处理参数。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamDealt
{
    /**
     * 参数处理错误的原因。
     */
    public interface FailedReason
    {
        JSONObject toJSON();

        String desc();
    }

    /**
     * 进行参数处理
     *
     * @param names           参数名称
     * @param values          用于放参数值
     * @param isNecessary     是否是必须参数
     * @param paramSource     参数原
     * @param typeParserStore 类型转换store
     * @return 转换成功返回null，否则返回对应的错误原因。
     */
    FailedReason deal(InNames.Name[] names, Object[] values, boolean isNecessary, ParamSource paramSource,
            TypeParserStore typeParserStore);


}
