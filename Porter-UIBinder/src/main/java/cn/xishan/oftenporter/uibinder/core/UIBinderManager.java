package cn.xishan.oftenporter.uibinder.core;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class UIBinderManager implements BinderDataSender
{


    final static class UITemp
    {
        Binder binder;
        String varName;

        UITemp(Binder binder, String varName)
        {
            this.binder = binder;
            this.varName = varName;
        }

        @Override
        public int hashCode()
        {
            return varName.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (o != null && (o instanceof UITemp))
            {
                UITemp temp = (UITemp) o;
                return varName.equals(temp.varName);
            } else
            {
                return false;
            }
        }

        public void release()
        {
            binder.release();
        }

    }

    private CommonMain commonMain;
    private UIPlatform uiPlatform;
    private FireBlock fireBlock;
    //<pathPrefix,>
    private Map<String, List<UINamesOccurStore>> uiMap;
    private ErrListener errListener;

    public UIBinderManager(UIPlatform uiPlatform, CommonMain commonMain)
    {
        this.uiPlatform = uiPlatform;
        this.commonMain = commonMain;
        uiMap = new HashMap<>();
    }


    public void setErrListener(ErrListener errListener)
    {
        this.errListener = errListener;
    }

    public UIPlatform getUIPlatform()
    {
        return uiPlatform;
    }

    public CommonMain getCommonMain()
    {
        return commonMain;
    }

    public synchronized void setFireBlock(FireBlock fireBlock)
    {
        this.fireBlock = fireBlock;
    }

    public FireBlock getFireBlock()
    {
        return fireBlock;
    }

    /**
     * 进行ui绑定。
     *
     * @param uiProvider
     */
    public synchronized void bind(String pName,UIProvider uiProvider)
    {
        CommonMain commonMain = getCommonMain();
        if(WPTool.notNullAndEmpty(pName)){
            commonMain=PorterMain.getMain(pName);
            if(commonMain==null){
                throw new RuntimeException(CommonMain.class.getSimpleName()+" with pName '"+pName+"' not found");
            }
        }
        if (uiProvider.getErrListener() == null)
        {
            uiProvider.setErrListener(errListener);
        }
        uiProvider.setDelivery(commonMain.getPLinker());
        uiProvider.search(getUIPlatform());
        UINamesOccurStore uiNamesOccurStore = new UINamesOccurStore(this, uiProvider);
        Prefix prefix = uiProvider.getPrefix();
        List<UINamesOccurStore> list = uiMap.get(prefix.pathPrefix);
        if (list == null)
        {
            list = new ArrayList<>(1);
            uiMap.put(prefix.pathPrefix, list);
        }
        list.add(uiNamesOccurStore);
        AppValues callbackValues = prefix.getCallbackValues();
        if (prefix.getCallbackMethod() != null)
        {
            commonMain.getPLinker().currentBridge()
                    .request(new PRequest(prefix.pathPrefix + prefix.getCallbackMethod()).addParamAll(callbackValues),
                            null);
        }
    }

    /**
     * 清理指定prefix的所有绑定。
     *
     * @param deletePrefix
     */
    public synchronized void delete(Prefix deletePrefix)
    {
        List<UINamesOccurStore> list = uiMap.remove(deletePrefix.pathPrefix);
        if (list != null)
        {
            for (UINamesOccurStore aList : list)
            {
                aList.clear();
            }
        }
    }

    /**
     * 清理指定prefix的指定id的绑定。
     *
     * @param deletePrefix
     * @param idString
     */
    public synchronized void delete(Prefix deletePrefix, String idString)
    {
        if (idString == null)
        {
            return;
        }
        List<UINamesOccurStore> list = uiMap.get(deletePrefix.pathPrefix);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                UINamesOccurStore aList = list.get(i);
                if (idString.equals(aList.uiProvider.getIdString()))
                {
                    aList.clear();
                    list.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * 清理所有的。
     */
    public synchronized void clear()
    {
        Iterator<List<UINamesOccurStore>> iterator = uiMap.values().iterator();
        while (iterator.hasNext())
        {
            List<UINamesOccurStore> list = iterator.next();
            for (UINamesOccurStore store : list)
            {
                store.clear();
            }
            iterator.remove();
        }
    }

    @Override
    public synchronized void sendBinderData(String pathPrefix, BinderData binderData)
    {
        List<UINamesOccurStore> list = uiMap.get(pathPrefix);
        if (list != null)
        {
            JResponse jResponse = binderData.toResponse();
            for (UINamesOccurStore aList : list)
            {
                aList.doResponse(pathPrefix, jResponse);
            }
        }
    }

}
