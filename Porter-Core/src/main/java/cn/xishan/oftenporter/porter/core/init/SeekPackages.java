package cn.xishan.oftenporter.porter.core.init;


import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import com.alibaba.fastjson.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * 存放扫描包或类的对象，用于扫描接口，且会设置所有含有@{@linkplain AutoSet}注解的静态变量。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class SeekPackages {
    private JSONArray jsonArray = new JSONArray();
    private Set<Class<?>> classesForSeek;
    private Set<Object> objectsForSeek;

    public SeekPackages() {
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
    public SeekPackages addClassPorter(Class<?>... clazzes) throws NullPointerException {
        int i = 0;
        for (Class<?> clazz : clazzes) {
            if (clazz == null) {
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
    public SeekPackages addObjectPorter(Object... objects) throws NullPointerException {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object == null) {
                throw new NullPointerException("null element at index " + i);
            }
            objectsForSeek.add(object);

        }

        return this;
    }

    public Set<Object> getObjectsForSeek() {
        return objectsForSeek;
    }

    public Set<Class<?>> getClassesForSeek() {
        return classesForSeek;
    }

    /**
     * 添加待扫描的包。
     *
     * @param packages 包名称，可变参数
     * @return
     */
    public SeekPackages addPorters(String... packages) {

        for (int i = 0; i < packages.length; i++) {
            jsonArray.add(packages[i]);
        }

        return this;
    }

    /**
     * 添加包。
     *
     * @param packages 存放的是字符串
     * @return
     */
    public SeekPackages addPorters(JSONArray packages) {
        jsonArray.addAll(packages);
        return this;
    }

    public JSONArray getPackages() {
        return jsonArray;
    }
}
