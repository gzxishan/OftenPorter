package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.WCallException;

/**
 * Created by chenyg on 2017/1/5.
 */
class PortExecutorCheckers extends CheckHandle
{

    private int currentIndex;
    private boolean inAll;
    private WObject wObject;
    private DuringType duringType;
    private CheckPassable[] checkPassables;
    private CheckHandle handle;
    private CheckPassable[] forAllCheckPassables;

    public PortExecutorCheckers(Context context, WObjectImpl wObject, DuringType duringType,
            CheckPassable[] checkPassables, CheckHandle handle)
    {
        super(handle);
        currentIndex = 0;
        this.wObject = wObject;
        this.duringType = duringType;
        this.checkPassables = checkPassables;
        this.forAllCheckPassables = context.forAllCheckPassables;
        this.handle = handle;
    }

    public PortExecutorCheckers(Context context, WObjectImpl wObject, DuringType duringType,
            CheckHandle handle,
            Class<? extends CheckPassable>[] ... cpss)
    {
        this(context, wObject, duringType, toCheckPassables(context, cpss), handle);
    }

    private static CheckPassable[] toCheckPassables(Context context, Class<? extends CheckPassable>[] ... cpss)
    {
        ContextPorter contextPorter = context.contextPorter;
        int totalLength=0;
        for (int i = 0; i < cpss.length; i++)
        {
            totalLength+=cpss[i].length;
        }
        CheckPassable[] checkPassables = new CheckPassable[totalLength];
        int k=0;
        for (int i = 0; i < cpss.length; i++)
        {
            Class<? extends CheckPassable>[] cps = cpss[i];
            for (int j = 0; j <cps.length ; j++)
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
        if (forAllCheckPassables != null)
        {
            inAll = true;
            checkForAll();
        } else
        {
            inAll = false;
            checkOne();
        }
    }

    private void checkForAll()
    {
        if (currentIndex < forAllCheckPassables.length)
        {
            forAllCheckPassables[currentIndex++].willPass(wObject, duringType, this);
        } else
        {
            inAll = false;
            currentIndex = 0;
            checkOne();
        }
    }

    private void checkOne()
    {
        if (currentIndex < checkPassables.length)
        {
            checkPassables[currentIndex++].willPass(wObject, duringType, this);
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
            if(failedObject instanceof WCallException){
                WCallException callException = (WCallException) failedObject;
                failedObject=callException.theJResponse();
            }
            handle.go(failedObject);
        } else if (inAll)
        {
            checkForAll();
        } else
        {
            checkOne();
        }
    }
}
