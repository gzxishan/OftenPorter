package cn.xishan.oftenporter.porter.core.init;


import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 存放扫描包或类的对象，用于扫描接口，且会设置所有含有@{@linkplain AutoSet}注解的静态变量。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class SeekPackages
{


    public static class Tiedfix{
        private String prefix;
        private String suffix;
        private boolean checkExists=true;

        public Tiedfix(String prefix, String suffix)
        {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * 返回为true时，如果绑定名已经包含了前缀或后缀则不会添加。默认为true。
         */
        public boolean isCheckExists()
        {
            return checkExists;
        }

        public void setCheckExists(boolean checkExists)
        {
            this.checkExists = checkExists;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getSuffix()
        {
            return suffix;
        }
    }

    /**
     * 可以给指定包下的接口类绑定名加前缀或后缀。
     */
    public static class TiedfixPkg
    {
        private Tiedfix classTiedfix;
        private String packageName;

        /**
         * @param classTiedfix      给所有Porter加上该前缀。
         * @param packageName
         */
        public TiedfixPkg(Tiedfix classTiedfix, String packageName)
        {
            if(OftenTool.notNullAndEmpty(classTiedfix.prefix)){
                PortUtil.checkName(classTiedfix.prefix);
            }
            if(OftenTool.notNullAndEmpty(classTiedfix.suffix)){
                PortUtil.checkName(classTiedfix.suffix);
            }

            this.classTiedfix = classTiedfix;
            this.packageName = packageName;
        }

        public Tiedfix getClassTiedfix()
        {
            return classTiedfix;
        }

        public String getPackageName()
        {
            return packageName;
        }
    }

    private JSONArray packages = new JSONArray();
    private Set<Class<?>> classesForSeek;
    private Set<Object> objectsForSeek;
    private List<TiedfixPkg> tiedfixPkgs = new ArrayList<>();

    public SeekPackages()
    {
        classesForSeek = new HashSet<>();
        objectsForSeek = new HashSet<>();
    }

    /**
     * 添加扫描的接口类。
     *
     * @param clazzes 待扫描的类
     * @return
     * @throws NullPointerException clazz为null。
     */
    public SeekPackages addClassPorter(Class<?>... clazzes) throws NullPointerException
    {
        int i = 0;
        for (Class<?> clazz : clazzes)
        {
            if (clazz == null)
            {
                throw new NullPointerException("null element at index " + i);
            }
            classesForSeek.add(clazz);
            i++;
        }

        return this;
    }

    /**
     * 添加扫描的接口对象。
     *
     * @param objects 待扫描的类
     * @return
     * @throws NullPointerException object为null。
     */
    public SeekPackages addObjectPorter(Object... objects) throws NullPointerException
    {
        for (int i = 0; i < objects.length; i++)
        {
            Object object = objects[i];
            if (object == null)
            {
                throw new NullPointerException("null element at index " + i);
            }
            objectsForSeek.add(object);

        }

        return this;
    }

    public Set<Object> getObjectsForSeek()
    {
        return objectsForSeek;
    }

    public Set<Class<?>> getClassesForSeek()
    {
        return classesForSeek;
    }

    /**
     * 添加待扫描的包。
     *
     * @param packages 包名称，可变参数
     * @return
     */
    public SeekPackages addPackages(String... packages)
    {

        for (int i = 0; i < packages.length; i++)
        {
            this.packages.add(packages[i]);
        }
        return this;
    }

    /**
     * 添加包。
     *
     * @param packages 存放的是字符串
     * @return
     */
    public SeekPackages addPackages(JSONArray packages)
    {
        packages.addAll(packages);
        return this;
    }

    public JSONArray getPackages()
    {
        return packages;
    }

    public SeekPackages addTiedfixPkg(TiedfixPkg tiedfixPkg)
    {
        tiedfixPkgs.add(tiedfixPkg);
        return this;
    }

    public List<TiedfixPkg> getTiedfixPkgs()
    {
        return tiedfixPkgs;
    }
}
