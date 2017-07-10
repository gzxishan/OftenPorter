package cn.xishan.oftenporter.oftendb.data;

import java.io.Closeable;

/**
 * @author Created by https://github.com/CLovinr on 2017/7/1.
 */
public interface Configing extends Closeable
{
    void setCollectionName(String collectionName);
}
