package cn.xishan.oftenporter.demo.core.test1.check;

import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;

class CheckPassableImpl implements CheckPassable {

    @Override
    public void willPass(WObject wObject, DuringType type, CheckHandle handle) {
        System.out.println(getClass().getName() + " is invoked:" + type);
        handle.next();
    }

}
