package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.WCallException;

/**
 * Created by chenyg on 2017/1/5.
 */
class PortExecutorCheckers extends CheckHandle {

    static abstract class CheckHandleAdapter extends CheckHandle{

        public CheckHandleAdapter(CheckHandle checkHandle) {
            super(checkHandle);
        }

        public CheckHandleAdapter(UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject, Object handleMethod, OutType outType, ABOption abOption) {
            super(urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
        }

        public CheckHandleAdapter(Object returnObj, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject, Object handleMethod, OutType outType, ABOption abOption) {
            super(returnObj, urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
        }

        public CheckHandleAdapter(Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject, Object handleMethod, OutType outType, ABOption abOption) {
            super(exCause, urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
        }



        @Override
        public Porter getClassPorter() {
            return null;
        }

        @Override
        public PorterOfFun getFunPorter() {
            return null;
        }
    }

    private int currentIndex;
    private boolean inAll;
    private WObject wObject;
    private DuringType duringType;
    private CheckPassable[] checkPassables;
    private CheckHandle handle;
    private CheckPassable[] forAllChecksNotZeroLen;

    private PorterOfFun porterOfFun;

    public PortExecutorCheckers(Context context, PorterOfFun porterOfFun, WObjectImpl wObject, DuringType duringType,
                                CheckPassable[] checkPassables, CheckHandle handle) {
        super(handle);
        this.porterOfFun = porterOfFun;
        currentIndex = 0;
        this.wObject = wObject;
        this.duringType = duringType;
        this.checkPassables = checkPassables;
        if (context != null) {
            this.forAllChecksNotZeroLen = context.forAllChecksNotZeroLen;
        }
        this.handle = handle;
    }

    public PortExecutorCheckers(Context context, PorterOfFun porterOfFun, WObjectImpl wObject, DuringType duringType,
                                CheckHandle handle,
                                Class<? extends CheckPassable>[]... cpss) {
        this(context, porterOfFun, wObject, duringType, toCheckPassables(context, cpss), handle);
    }

    private static CheckPassable[] toCheckPassables(Context context, Class<? extends CheckPassable>[]... cpss) {
        ContextPorter contextPorter = context.contextPorter;
        int totalLength = 0;
        for (int i = 0; i < cpss.length; i++) {
            totalLength += cpss[i].length;
        }
        CheckPassable[] checkPassables = new CheckPassable[totalLength];
        int k = 0;
        for (int i = 0; i < cpss.length; i++) {
            Class<? extends CheckPassable>[] cps = cpss[i];
            for (int j = 0; j < cps.length; j++) {
                CheckPassable cp = contextPorter.getCheckPassable(cps[j]);
                checkPassables[k++] = cp;
            }
        }
        return checkPassables;
    }

    public void check() {
        currentIndex = 0;
        if (forAllChecksNotZeroLen != null) {
            inAll = true;
            checkForAll();
        } else {
            inAll = false;
            checkOne();
        }
    }

    private void checkForAll() {
        if (currentIndex < forAllChecksNotZeroLen.length) {
            forAllChecksNotZeroLen[currentIndex++].willPass(wObject, duringType, this);
        } else {
            inAll = false;
            currentIndex = 0;
            checkOne();
        }
    }

    private void checkOne() {
        if (currentIndex < checkPassables.length) {
            checkPassables[currentIndex++].willPass(wObject, duringType, this);
        } else {
            handle.next();
        }
    }

    @Override
    public void go(Object failedObject) {
        if (failedObject != null) {
            if (failedObject instanceof WCallException) {
                WCallException callException = (WCallException) failedObject;
                failedObject = callException.theJResponse();
            }
            handle.go(failedObject);
        } else if (inAll) {
            checkForAll();
        } else {
            checkOne();
        }
    }


    @Override
    public Porter getClassPorter() {
        return porterOfFun.getPorter();
    }

    @Override
    public PorterOfFun getFunPorter() {
        return porterOfFun;
    }
}
