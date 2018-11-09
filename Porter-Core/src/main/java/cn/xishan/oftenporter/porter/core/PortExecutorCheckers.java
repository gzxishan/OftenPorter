package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyg on 2017/1/5.
 */
class PortExecutorCheckers extends CheckHandle
{

    static abstract class CheckHandleAdapter extends CheckHandle
    {

        public CheckHandleAdapter(CheckHandle checkHandle)
        {
            super(checkHandle);
        }

        public CheckHandleAdapter(UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
                Object handleMethod, OutType outType)
        {
            super(urlResult, finalPorterObject, handleObject, handleMethod, outType);
        }

        public CheckHandleAdapter(Object returnObj, UrlDecoder.Result urlResult, Object finalPorterObject,
                Object handleObject, Object handleMethod, OutType outType)
        {
            super(returnObj, urlResult, finalPorterObject, handleObject, handleMethod, outType);
        }

        public CheckHandleAdapter(Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject,
                Object handleObject, Object handleMethod, OutType outType)
        {
            super(exCause, urlResult, finalPorterObject, handleObject, handleMethod, outType);
        }


        @Override
        public Porter getClassPorter()
        {
            return null;
        }

        @Override
        public PorterOfFun getFunPorter()
        {
            return null;
        }
    }

    private int currentIndex;
    //private boolean inPorters;
    private WObject wObject;
    private DuringType duringType;
    //private CheckPassable[] checkPassables;
    private CheckHandle handle;
    //private CheckPassable[] porterCheckPassables;
    private List<CheckPassable> checkPassableList;

    private PorterOfFun porterOfFun;


    public PortExecutorCheckers(Context context, PorterOfFun porterOfFun, WObjectImpl wObject, DuringType duringType,
            CheckPassable[] checkPassables, CheckHandle handle)
    {
        this(context, porterOfFun, wObject, duringType, checkPassables, handle, false);
    }

    private PortExecutorCheckers(Context context, PorterOfFun porterOfFun, WObjectImpl wObject, DuringType duringType,
            CheckPassable[] checkPassables, CheckHandle handle, boolean isPorterCheckPassablesFirst)
    {
        super(handle);
        this.porterOfFun = porterOfFun;
        currentIndex = 0;
        this.wObject = wObject;
        this.duringType = duringType;
        checkPassableList = new ArrayList<>(
                checkPassables.length + (context != null && context.porterCheckPassables != null ? context
                        .porterCheckPassables.length : 0));
        if (isPorterCheckPassablesFirst)
        {
            if (context != null && context.porterCheckPassables != null)
            {
                WPTool.addAll(checkPassableList, context.porterCheckPassables);
            }
            WPTool.addAll(checkPassableList, checkPassables);
        } else
        {
            WPTool.addAll(checkPassableList, checkPassables);
            if (context != null)
            {
                WPTool.addAll(checkPassableList, context.porterCheckPassables);
            }
        }

        this.handle = handle;
    }

    public PortExecutorCheckers(Context context, PorterOfFun porterOfFun, WObjectImpl wObject, DuringType duringType,
            CheckHandle handle,
            Class<? extends CheckPassable>[]... cpss)
    {
        this(context, porterOfFun, wObject, duringType, toCheckPassables(context, cpss), handle, true);
    }

    private static CheckPassable[] toCheckPassables(Context context, Class<? extends CheckPassable>[]... cpss)
    {
        ContextPorter contextPorter = context.contextPorter;
        int totalLength = 0;
        for (int i = 0; i < cpss.length; i++)
        {
            totalLength += cpss[i].length;
        }
        CheckPassable[] checkPassables = new CheckPassable[totalLength];
        int k = 0;
        for (int i = 0; i < cpss.length; i++)
        {
            Class<? extends CheckPassable>[] cps = cpss[i];
            for (int j = 0; j < cps.length; j++)
            {
                CheckPassable cp = contextPorter.getCheckPassable(cps[j]);
                checkPassables[k++] = cp;
            }
        }
        return checkPassables;
    }

    public void check()
    {
        currentIndex = 0;
        checkOne();
    }


    private void checkOne()
    {
        if (currentIndex < checkPassableList.size())
        {
            int index = currentIndex++;
            if (duringType == DuringType.AFTER_METHOD || duringType == DuringType.ON_METHOD_EXCEPTION)
            {
                index = checkPassableList.size() - index - 1;
            }
            checkPassableList.get(index).willPass(wObject, duringType, this);
        } else
        {
            handle.next();
        }
    }

    @Override
    public void go(Object failedObject)
    {
        if (failedObject != null)
        {
            if (failedObject instanceof OftenCallException)
            {
                OftenCallException callException = (OftenCallException) failedObject;
                failedObject = callException.theJResponse();
            }
            handle.go(failedObject);
        } else
        {
            checkOne();
        }
    }


    @Override
    public Porter getClassPorter()
    {
        return porterOfFun.getPorter();
    }

    @Override
    public PorterOfFun getFunPorter()
    {
        return porterOfFun;
    }
}
