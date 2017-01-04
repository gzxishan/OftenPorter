package cn.xishan.oftenporter.uibinder.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.uibinder.core.UIBinderManager.UITemp;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
class UINamesOccurStore
{
    //<接口方法绑定名，<参数名，UITemp>>
    private HashMap<String, HashMap<String, UITemp>> namesMap = new HashMap<>(5);
    private HashMap<String, UITemp> occursMap = new HashMap<>(3);
    UIProvider uiProvider;
    private UIBinderManager uiBinderManager;

    public UINamesOccurStore(UIBinderManager uiBinderManager, UIProvider uiProvider)
    {
        this.uiBinderManager = uiBinderManager;
        this.uiProvider = uiProvider;
        Iterator<UiId> iterator = uiProvider.getUIs().iterator();
        while (iterator.hasNext())
        {
            UiId uiId = iterator.next();
            Binder binder = uiProvider.getBinder(uiId);
            if(binder!=null){
                seek(uiId, binder, uiProvider.getPrefix());
            }
        }
    }

    /**
     * 清理
     */
    public void clear()
    {
        Iterator<UITemp> iterator = occursMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().release();
        }
        Iterator<HashMap<String, UITemp>>
                iterator1 =
                namesMap.values().iterator();
        while (iterator1.hasNext())
        {
            Iterator<UITemp> iterator2 = iterator1.next().values().iterator();
            while (iterator2.hasNext())
            {
                UITemp temp = iterator2.next();
                temp.release();
            }
        }
        namesMap.clear();
        occursMap.clear();
    }

    private void addBinder(String tiedFun, String paramName,
            Binder binder)
    {
        if (tiedFun == null)
            return;
        HashMap<String, UITemp> map = namesMap.get(tiedFun);
        if (map == null)
        {
            map = new HashMap<>();
            namesMap.put(tiedFun, map);
        }
        map.put(paramName, new UITemp(binder, paramName));
    }

    private void seekOfOccur(Binder binder, IdDeal.Result result)
    {
        Binder.PorterOccur porterOccur = new Binder.PorterOccur()
        {
            @Override
            public void doPorter(String pathPrefix, String tiedFun, PortMethod method)
            {
                FireBlock fireBlock = uiBinderManager.getFireBlock();
                if (fireBlock != null && !fireBlock.willFire(pathPrefix, tiedFun))
                {
                    return;
                }
                onOccur(pathPrefix, tiedFun, method);
            }


            private void onLocalOccur(String pathPrefix, final String tiedFun, final PortMethod method)
            {
                Binder[] binders = nameBindersOfTiedFun(tiedFun);
                UIListenerAndCallback uiListenerAndCallback = new UIListenerAndCallback(UINamesOccurStore.this, method,
                        pathPrefix,
                        tiedFun);
                uiProvider.getUIAttrGetter().onGet(uiListenerAndCallback, tiedFun, binders, AttrEnum.ATTR_VALUE);
            }

            private void onOccur(final String pathPrefix, final String tiedFun, final PortMethod method)
            {
//                do
//                {
//                    if (!(prefix instanceof SimpleDealtPrefix))
//                    {
//                        break;
//                    }
//                    final SimpleDealtPrefix simpleDealtPrefix = (SimpleDealtPrefix) prefix;
//                    if (simpleDealtPrefix.inExcept(tiedFun))
//                    {
//                        break;
//                    }
//                    //自动转发到其他地方处理
//
//                    Binder[] binders = nameBindersOfTiedFun(tiedFun);
//                    uiAttrGetter.onGet(new UIAttrGetter.Listener()
//                    {
//                        @Override
//                        public void onGet(AppValues appValues)
//                        {
//                            simpleDealtPrefix.doOccur(prefixStr, tiedFun, appValues, method);
//                        }
//                    }, binders, AttrEnum.ATTR_VALUE);
//                    return;
//
//                } while (false);

                //本地接口处理
                onLocalOccur(pathPrefix, tiedFun, method);
            }
        };

        binder.set(result, porterOccur);
        String[] funs = result.getFunNames();
        for (int i = 0; i < funs.length; i++)
        {
            occursMap.put(funs[i], new UITemp(binder, funs[i]));
        }
    }

    private void seek(UiId id, Binder binder, Prefix prefix)
    {
        IdDeal.Result result = uiBinderManager.getUIPlatform().getIdDeal().dealId(id, prefix.pathPrefix);

        if (result == null)
        {
            //TODO
            // binder.onInitFailed();
            return;
        }

        if (result.isOccur())
        {
            seekOfOccur(binder, result);
        } else
        {
            binder.set(result, null);

            String[] funs = result.getFunNames();
            for (int i = 0; i < funs.length; i++)
            {
                String tiedFun = funs[i];
                addBinder(tiedFun, result.getVarName(), binder);
            }

        }

        binder.onInitOk();

    }

    private static final Binder[] EMPTY_BINDER = new Binder[0];

    private Binder[] nameBindersOfTiedFun(String tiedFun)
    {
        HashMap<String, UITemp> map = namesMap.get(tiedFun);
        Binder[] binders;
        if (map != null)
        {
            binders = new Binder[map.size()];
            Iterator<UITemp> iterator = map.values().iterator();
            int i = 0;
            while (iterator.hasNext())
            {
                UITemp temp = iterator.next();
                binders[i++] = temp.binder;
            }
        } else
        {
            binders = EMPTY_BINDER;
        }
        return binders;
    }


    /**
     * 用于处理获取值
     *
     * @param task
     */
    private void doGetValue(BinderData.Task task)
    {
        final BinderData.GetTask getTask = (BinderData.GetTask) task.data;
        List<BinderGet> list = getTask.binderGets;

        Binder[] binders = new Binder[list.size()];
        AttrEnum[] types = new AttrEnum[list.size()];

        for (int i = 0; i < binders.length; i++)
        {
            BinderGet binderGet = list.get(i);
            HashMap<String, UITemp>
                    hashMap =
                    this.namesMap.get(binderGet.tiedFunName);

            UITemp temp = hashMap == null ? null : hashMap.get(binderGet.paramName);
            if (temp == null)
            {
                temp = occursMap.get(binderGet.paramName);
            }
            if (temp != null)
            {
                binders[i] = temp.binder;
                types[i] = binderGet.varType;
            }
        }
        uiProvider.getUIAttrGetter().onGet(new UIAttrGetter.Listener()
        {
            @Override
            public void onGet(AppValues appValues)
            {
                getTask.binderGetListener.onGet(appValues.getValues());
            }
        }, null, binders, types);

    }

    private void setValue(List<BinderSet> list)
    {

        for (int i = 0; i < list.size(); i++)
        {
            BinderSet binderSet = list.get(i);
            HashMap<String, UITemp>
                    hashMap =
                    this.namesMap.get(binderSet.tiedFunName);
            UITemp temp = hashMap == null ?
                    null :
                    hashMap.get(binderSet.paramName);
            if (temp != null)
            {
                temp.binder.set(binderSet.attrEnum, binderSet.value);
            } else
            {
                temp = occursMap.get(binderSet.paramName);
                if (temp != null)
                {
                    temp.binder.set(binderSet.attrEnum, binderSet.value);
                }
            }
        }
    }

    /**
     * @param pathPrefix 接口前缀
     * @param jResponse
     */
    void doResponse(String pathPrefix,
            JResponse jResponse)
    {
        try
        {
            BinderData binderData = (BinderData) jResponse.getResult();
            List<BinderData.Task> list = binderData.getTasks();
            for (int i = 0; i < list.size(); i++)
            {
                BinderData.Task task = list.get(i);
                if (AttrEnum.METHOD_ASYNC_SET == task.method)
                {
                    List<BinderSet> _list = (List<BinderSet>) task.data;
                    setValue(_list);
                } else if (AttrEnum.METHOD_ASYNC_GET == task.method)
                {
                    doGetValue(task);
                }
            }

        } catch (Exception e)
        {
            ErrListener errListener = uiProvider.getErrListener();
            if (errListener != null)
            {
                errListener.onException(e, pathPrefix);
            }
        }
    }
}
