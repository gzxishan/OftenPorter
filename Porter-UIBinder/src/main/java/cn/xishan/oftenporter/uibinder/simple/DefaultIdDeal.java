package cn.xishan.oftenporter.uibinder.simple;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.uibinder.core.IdDeal;
import cn.xishan.oftenporter.uibinder.core.UiId;

import java.util.ArrayList;

/**
 * <pre>
 *     1.以“_”结尾的是用来触发的，以“__”结尾的表示最后一个是请求方法
 *     2.全部以“_”分割
 *     例子，id前缀为test_
 *     1)test_name_setName_setInfo:控件绑定的变量名为name,与方法setName和setInfo绑定值一起
 *     2)test_getName_refresh_:控件与getName与refresh两个触发进行绑定。
 *     3)test_getName_refresh_POST__:控件与getName,refresh进行绑定,请求方法为POST。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class DefaultIdDeal implements IdDeal
{
    @Override
    public Result dealId(UiId id, String pathPrefix)
    {
        if (WPTool.isEmpty(id.getId()))
        {
            return null;
        }

        final String idStr = id.getId();

        String[] strIds = StrUtil.split(idStr, "_");
        if (strIds.length == 0)
        {
            return null;
        }

        Result result = new Result();

        result.setIsOccur(idStr.endsWith("_"));
        ArrayList<String> list = new ArrayList<>(strIds.length);
        if (result.isOccur())
        {

            for (int i = 0; i < strIds.length - 1; i++)
            {
                if (WPTool.notNullAndEmpty(strIds[0]))
                {
                    list.add(strIds[i]);
                }
            }

            PortMethod method;
            if (id.getId().endsWith("__"))
            {
                try
                {
                    method = PortMethod.valueOf(strIds[strIds.length - 1]);
                } catch (IllegalArgumentException e)
                {
                    throw e;
                }
            } else
            {
                method = PortMethod.GET;
                list.add(strIds[strIds.length - 1]);
            }

            result.setMethod(method);
            result.setFunNames(list.toArray(new String[0]));
        } else
        {
            result.setVarName(strIds[0]);
            for (int i = 1; i < strIds.length; i++)
            {
                if (WPTool.notNullAndEmpty(strIds[0]))
                {
                    list.add(strIds[i]);
                }
            }
            result.setFunNames(list.toArray(new String[0]));
        }

        result.setPathPrefix(pathPrefix);
        return result;
    }
}
