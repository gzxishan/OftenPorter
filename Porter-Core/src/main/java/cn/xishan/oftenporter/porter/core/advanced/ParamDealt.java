package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.WObject;
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
     * @param neceDeals       如果不为null，可以通过{@linkplain _Nece#isNece(WObject)}来判断是否最终为必需参数。
     * @param values          用于放参数值
     * @param isNecessary     是否是必须参数
     * @param paramSource     参数原
     * @param typeParserStore 类型转换store
     * @param namePrefix      参数前缀名称
     * @return 转换成功返回null，否则返回对应的错误原因。
     */

    FailedReason deal(@MayNull WObject wObject, InNames.Name[] names, @MayNull _Nece[] neceDeals, Object[] values,
            boolean isNecessary,ParamSource paramSource,TypeParserStore typeParserStore, String namePrefix);


}
