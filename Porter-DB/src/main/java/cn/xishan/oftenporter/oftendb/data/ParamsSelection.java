package cn.xishan.oftenporter.oftendb.data;

/**
 * 参数选择,用来构造简单的查询条件(name1=value1 AND name2=value2...)。
 * <pre>
 * 索引格式如下：
 * 1.非负数表示对数据无操作；负数则表示，要将值置空。
 * 2.非负数对应数组的索引；而负数，则是先加1，再取绝对值，才对应数组索引。
 *
 * 若使用的是@Key则会根据相关规确定最终键的名称。
 * </pre>
 *
 * @author ZhuiFeng
 */
public class ParamsSelection
{
    int[] nIndexes;
    int[] uIndexes;

    /**
     * 选择必须参数中的值，用来构造查询条件。
     *
     * @param indexes
     * @return
     */
    public ParamsSelection nIndexes(int... indexes)
    {
        this.nIndexes = indexes;
        return this;
    }

    /**
     * 选择非必须参数中的值，用来构造查询条件。
     *
     * @param indexes
     * @return
     */
    public ParamsSelection uIndexes(int... indexes)
    {
        this.uIndexes = indexes;
        return this;
    }
}
