package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by https://github.com/CLovinr on 2016/9/9.
 */
public abstract class DataAble implements Cloneable {
    public static final int OPTION_CODE_DEFAULT = -1;
    public static final int OPTION_CODE_EXISTS = -2;
    public static final int OPTION_CODE_LOGIN = -3;

    private String collectionName;

    /**
     * 得到所属集合(或表)名称。
     *
     * @return
     */
    public final void setCollectionName(String collName) {
        this.collectionName = collName;
    }

    public final String getCollectionName() {
        return collectionName;
    }

    public abstract Condition forQuery();

    public abstract KeysSelection keys();

    public abstract JSONObject toJsonObject();

    // public abstract Field getField(String fieldName)throws NoSuchFieldException;

    protected abstract NameValues toNameValues(ParamsGetter.Params params) throws Exception;

    public abstract void whenSetDataFinished(SetType setType, int optionCode, WObject wObject,
                                             DBHandleAccess dbHandleAccess) throws DataException;

    protected abstract void setParams(InNames.Name[] neceFields, Object[] nvalues, InNames.Name[] unneceFields,
                                      Object[] uvalues, InNames.Name
                                              [] innerNames, Object[] innerValues) throws Exception;

    /**
     * 得到数据库选择的键。
     *
     * @return
     */
    protected abstract String[] getFinalKeys(KeysSelection keysSelection, ParamsGetter.Params params);

    protected abstract void dealNames(Condition condition);

    protected abstract void dealNames(QuerySettings querySettings);

    protected abstract DataAble cloneData();

    /**
     * 转换ParamsSelection为Condition
     *
     * @param dbHandleSource
     * @param selection
     * @param wObject
     * @param params
     * @return
     */
    protected abstract Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WObject wObject,
                                          ParamsGetter.Params params);
}
