package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * Created by cheyg on 2017/1/5.
 */
class PortExecutorCheckers extends CheckHandle {

    private int currentIndex;
    private WObject wObject;
    private DuringType duringType;
    private CheckPassable[] checkPassables;
    private CheckHandle handle;

    public PortExecutorCheckers(WObject wObject, DuringType duringType, CheckPassable[] checkPassables, CheckHandle handle) {
        currentIndex = 0;
        this.wObject = wObject;
        this.duringType = duringType;
        this.checkPassables = checkPassables;
        this.handle = handle;
    }

    public PortExecutorCheckers(Context context, WObject wObject, DuringType duringType, Class<? extends CheckPassable>[] cps, CheckHandle handle) {
        this(wObject, duringType, toCheckPassables(context, cps), handle);
    }

    private static CheckPassable[] toCheckPassables(Context context, Class<? extends CheckPassable>[] cps) {
        PortContext portContext = context.portContext;
        CheckPassable[] checkPassables = new CheckPassable[cps.length];
        for (int i = 0; i < cps.length; i++) {
            CheckPassable cp = portContext.getCheckPassable(cps[i]);
            checkPassables[i] = cp;

        }
        return checkPassables;
    }

    public void check() {
        if (currentIndex < checkPassables.length) {
            checkPassables[currentIndex++].willPass(wObject, duringType, this);
        } else {
            handle.next();
        }
    }

    @Override
    public void go(Object failedObject) {
        if (failedObject != null) {
            handle.go(failedObject);
        } else {
            check();
        }
    }
}
