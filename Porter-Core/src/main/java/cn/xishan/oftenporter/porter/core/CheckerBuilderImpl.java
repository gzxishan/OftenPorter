package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.sysset.CheckerBuilder;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2019-12-19.
 */
class CheckerBuilderImpl implements CheckerBuilder
{
    private static class HandleAdapter extends CheckHandle
    {
        private Handle handle;

        public HandleAdapter(UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
                Object handleMethod, OutType outType, Handle handle)
        {
            super(urlResult, finalPorterObject, handleObject, handleMethod, outType);
            this.handle = handle;
        }

        public HandleAdapter(Object returnObj, UrlDecoder.Result urlResult, Object finalPorterObject,
                Object handleObject, Object handleMethod, OutType outType, Handle handle)
        {
            super(returnObj, urlResult, finalPorterObject, handleObject, handleMethod, outType);
            this.handle = handle;
        }

        public HandleAdapter(Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject,
                Object handleObject, Object handleMethod, OutType outType, Handle handle)
        {
            super(exCause, urlResult, finalPorterObject, handleObject, handleMethod, outType);
            this.handle = handle;
        }

        @Override
        public void go(Object failedObject)
        {
            this.handle.handle(failedObject);
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


    private static class CheckerImpl extends CheckHandle implements Checker
    {
        private int currentIndex;
        //private boolean inPorters;
        private OftenObject oftenObject;
        private DuringType duringType;
        //private CheckPassable[] checkPassables;
        private CheckHandle handle;
        //private CheckPassable[] porterCheckPassables;
        private List<CheckPassable> checkPassableList;

        private PorterOfFun porterOfFun;

        public CheckerImpl(BuilderImpl builder, CheckHandle checkHandle, CheckPassable[] checkPassables)
        {
            this(builder.context, builder.porterOfFun, builder.oftenObject, builder.duringType,
                    checkPassables, checkHandle, false);
        }

        public CheckerImpl(BuilderImpl builder, CheckHandle checkHandle, Class<? extends CheckPassable>[]... cpss)
        {
            this(builder.context, builder.porterOfFun, builder.oftenObject, builder.duringType,
                    toCheckPassables(builder.context, cpss), checkHandle, true);
        }

        private CheckerImpl(Context context, PorterOfFun porterOfFun, OftenObject oftenObject,
                DuringType duringType,
                CheckPassable[] checkPassables, CheckHandle handle, boolean isPorterCheckPassablesFirst)
        {
            super(handle);
            this.porterOfFun = porterOfFun;
            currentIndex = 0;
            this.oftenObject = oftenObject;
            this.duringType = duringType;
            checkPassableList = new ArrayList<>(
                    checkPassables.length + (context != null && context.porterCheckPassables != null ? context
                            .porterCheckPassables.length : 0));
            if (isPorterCheckPassablesFirst)
            {
                if (context != null && context.porterCheckPassables != null)
                {
                    OftenTool.addAll(checkPassableList, context.porterCheckPassables);
                }
                OftenTool.addAll(checkPassableList, checkPassables);
            } else
            {
                OftenTool.addAll(checkPassableList, checkPassables);
                if (context != null)
                {
                    OftenTool.addAll(checkPassableList, context.porterCheckPassables);
                }
            }

            this.handle = handle;
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


        @Override
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
                {//逆向执行
                    index = checkPassableList.size() - index - 1;
                }
                checkPassableList.get(index).willPass(oftenObject, duringType, this);
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
                if (failedObject instanceof Throwable)
                {
                    failedObject = OftenTool.getCause((Throwable) failedObject);
                }
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
            return porterOfFun == null ? null : porterOfFun.getPorter();
        }

        @Override
        public PorterOfFun getFunPorter()
        {
            return porterOfFun;
        }
    }

    class BuilderImpl implements Builder
    {
        private OftenObject oftenObject;
        private Handle handle;
        private Context context;

        private Object withReturn;
        private Throwable withThrowable;
        private UrlDecoder.Result withUrl;

        private OutType outType;
        private Object finalPorterObject;
        private Object handleObject;
        private Object handleMethod;
        private int withType = -1;

        private DuringType duringType;
        private PorterOfFun porterOfFun;


        public BuilderImpl(DuringType duringType)
        {
            this.duringType = duringType;
        }

        @Override
        public Builder setObject(OftenObject object, Object handleObject, Object handleMethod)
        {
            this.oftenObject = object;
            this.finalPorterObject = handleObject;
            this.handleObject = handleObject;
            this.handleMethod = handleMethod;
            return this;
        }

        @Override
        public Builder setObject(OftenObject object)
        {
            this.oftenObject = object;
            return this;
        }

        @Override
        public Builder setPorterOfFun(PorterOfFun porterOfFun)
        {
            this.porterOfFun = porterOfFun;
            this.finalPorterObject = porterOfFun.getFinalPorterObject();
            this.handleObject = porterOfFun.getObject();
            this.handleMethod = porterOfFun.getMethod();
            this.outType = porterOfFun.getPortOut().getOutType();
            return this;
        }

        @Override
        public Builder setContext(Context context)
        {
            this.context = context;
            return this;
        }

        @Override
        public Builder setHandle(Handle handle)
        {
            if (handle == null)
            {
                throw new NullPointerException("handle is null");
            }
            this.handle = handle;
            return this;
        }

        @Override
        public Builder withThrowable(UrlDecoder.Result url, Throwable throwable)
        {
            this.withType = 1;
            this.withThrowable = throwable;
            this.withReturn = null;
            this.withUrl = url;
            return this;
        }

        @Override
        public Builder withUrl(UrlDecoder.Result url)
        {
            this.withType = 2;
            this.withThrowable = null;
            this.withReturn = null;
            this.withUrl = url;
            return this;
        }

        @Override
        public Builder withReturn(UrlDecoder.Result url, Object returnObject)
        {
            this.withType = 3;
            this.withThrowable = null;
            this.withReturn = returnObject;
            this.withUrl = url;
            return this;
        }

        @Override
        public Builder setOutType(OutType outType)
        {
            this.outType = outType;
            return this;
        }

        @Override
        public Checker build()
        {
            CheckHandle checkHandle;

            if (handle == null)
            {
                throw new NullPointerException("handle is null");
            }

            switch (withType)
            {
                case 1://withThrowable
                    checkHandle = new HandleAdapter(withThrowable, withUrl, finalPorterObject, handleObject,
                            handleMethod, outType, handle);
                    break;
                case 2://withUrl
                    checkHandle = new HandleAdapter(withUrl, finalPorterObject, handleObject, handleMethod, outType,
                            handle);
                    break;
                case 3://withReturn
                    checkHandle = new HandleAdapter(withReturn, withUrl, finalPorterObject, handleObject, handleMethod,
                            outType, handle);
                    break;
                default:
                    throw new RuntimeException("not invoke withXXX");
            }

            Checker checker = null;
            switch (duringType)
            {
                case ON_GLOBAL:
                {
                    checker = new CheckerImpl(this, checkHandle, portExecutor.allGlobalChecks);
                }
                break;
                case ON_CONTEXT:
                {
                    CheckPassable[] contextChecks = context.contextChecks;
                    context = null;
                    checker = new CheckerImpl(this, checkHandle, contextChecks);
                }
                break;
                case BEFORE_CLASS:
                case ON_CLASS:
                {
                    if (porterOfFun != null)
                    {
                        Porter classPort = porterOfFun.getPorter();
                        _PortIn clazzPIn = classPort.getPortIn();
                        checker = new CheckerImpl(this, checkHandle,
                                classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                                clazzPIn.getChecks());
                    } else
                    {
                        checker = new CheckerImpl(this, checkHandle);
                    }
                }
                break;
                case BEFORE_METHOD:
                case ON_METHOD:
                case AFTER_METHOD:
                {
                    if (porterOfFun != null)
                    {
                        _PortIn funPIn = porterOfFun.getMethodPortIn();
                        checker = new CheckerImpl(this, checkHandle,
                                porterOfFun.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                                funPIn.getChecks());
                    } else
                    {
                        checker = new CheckerImpl(this, checkHandle);
                    }
                }
                break;
                case ON_METHOD_EXCEPTION:
                {
                    if (porterOfFun != null)
                    {
                        _PortIn funPIn = porterOfFun.getMethodPortIn();
                        checker = new CheckerImpl(this, checkHandle,
                                funPIn.getChecks(),
                                porterOfFun.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass());
                    } else
                    {
                        checker = new CheckerImpl(this, checkHandle);
                    }
                }
                break;
            }


            return checker;
        }
    }

    @Override
    public Builder newBuilder(DuringType duringType)
    {
        return new BuilderImpl(duringType);
    }

    @Override
    public Builder newBuilder(DuringType duringType, String contextName)
    {
        Context context = portExecutor.getContext(contextName);
        return newBuilder(duringType).setContext(context);
    }

    private PortExecutor portExecutor;


    public CheckerBuilderImpl(PortExecutor portExecutor)
    {
        this.portExecutor = portExecutor;
    }
}
