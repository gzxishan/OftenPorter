package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyg on 2017-03-24.
 */
class WholeClassCheckPassableGetterImpl implements WholeClassCheckPassableGetter
{
    List<Class<? extends CheckPassable>> mixinCheckForWholeClassList = new ArrayList<>();
    Class<? extends CheckPassable>[] cps;

    public void addAll(Class<? extends CheckPassable>[] cps)
    {
        WPTool.addAll(mixinCheckForWholeClassList, cps);
    }

    public void done()
    {
        cps = mixinCheckForWholeClassList.toArray(new Class[0]);
        mixinCheckForWholeClassList = null;
    }

    @Override
    public Class<? extends CheckPassable>[] getChecksForWholeClass()
    {
        return cps;
    }
}
