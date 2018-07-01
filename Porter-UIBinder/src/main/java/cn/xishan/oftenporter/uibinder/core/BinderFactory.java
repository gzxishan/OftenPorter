package cn.xishan.oftenporter.uibinder.core;


import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 宇宙之灵 on 2015/10/1.
 */
public class BinderFactory
{

    private class Temp
    {
        HashMap<Class<?>, Class<? extends Binder<?>>> binderHashMap = new HashMap<>();
        int n;

        Temp(int n)
        {
            this.n = n;
        }
    }


    private ArrayList<Temp> list = new ArrayList<Temp>();
    private Class<?>[] baseViewTypes;
    private Map<Class<?>, Constructor<Binder<?>>> cacheConstructorMap;

    public BinderFactory(Class<?>... baseViewTypes)
    {
        if (baseViewTypes == null || baseViewTypes.length == 0) throw new NullPointerException();
        this.baseViewTypes = baseViewTypes;
        cacheConstructorMap = new HashMap<>();
    }

    private int subClassOf(Class<?> clazz){
        int n = -1;
        for (int i = 0; i < baseViewTypes.length; i++)
        {
            n = WPTool.subclassOf(clazz, baseViewTypes[i]);
            if (n >= 0)
            {
                break;
            }
        }
        return n;
    }

    public BinderFactory put(Class<?> viewType, Class<? extends Binder<?>> binderClass)
    {

        int n = subClassOf(viewType);

        // if (n == -1) throw new RuntimeException(viewType + " is not the type of " + baseViewType);
        HashMap<Class<?>, Class<? extends Binder<?>>> binderHashMap;
        if (n >= list.size())
        {
            for (int i = n - list.size(); i >= 0; i--)
            {
                list.add(null);
            }
            Temp temp = new Temp(n);
            binderHashMap = temp.binderHashMap;
            list.set(n, temp);
        } else
        {
            Temp tmp = list.get(n);
            if (tmp == null)
            {
                tmp = new Temp(n);
                list.set(n, tmp);
            }
            binderHashMap = tmp.binderHashMap;
        }

        binderHashMap.put(viewType, binderClass);
        return this;
    }


    public synchronized <T> Binder<T> getBinder(T t)
    {
        Class<?> clazz = PortUtil.getRealClass(t);
        Constructor<Binder<?>> constructor = cacheConstructorMap.get(clazz);
        if (constructor != null)
        {
            try
            {
                Binder binder = constructor.newInstance(t);
                return binder;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        int n = subClassOf(clazz);
        Binder<T> binder = null;
        if (n >= list.size())
        {
            n = list.size() - 1;
        }

        outer:
        for (int i = n; i >= 0; i--)
        {
            Temp temp = list.get(i);
            if (temp == null) continue;
            HashMap<Class<?>, Class<? extends Binder<?>>> binderHashMap = temp.binderHashMap;
            Iterator<Class<?>> keys = binderHashMap.keySet().iterator();
            while (keys.hasNext())
            {
                Class<?> key = keys.next();
                if (WPTool.subclassOf(clazz, key) >= 0)
                {
                    try
                    {
                        Class<? extends Binder<?>> c = binderHashMap.get(key);
                        Constructor<?>[] constructors = c.getDeclaredConstructors();
                        for (Constructor<?> con : constructors)
                        {
                            Class<?>[] cs = con.getParameterTypes();
                            if (cs.length == 1 && subClassOf(cs[0]) >= 0)
                            {
                                con.setAccessible(true);
                                cacheConstructorMap.put(clazz, (Constructor<Binder<?>>) con);
                                binder = (Binder<T>) con.newInstance(t);
                                break outer;
                            }
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }

            }

        }
        return binder;
    }
}
