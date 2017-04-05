package cn.xishan.oftenporter.oftendb.data;

/**
 * Created by 刚帅 on 2016/1/19.
 */
public interface DBSource extends DBHandleSource, ParamsGetter
{
    DBSource withAnotherData(Class<? extends Data> clazz);
}
