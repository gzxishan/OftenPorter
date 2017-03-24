package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.base.CheckPassable;

/**
 * Created by chenyg on 2017-03-24.
 */
public interface WholeClassCheckPassableGetter
{
    Class<? extends CheckPassable>[] getChecksForWholeClass();
}
