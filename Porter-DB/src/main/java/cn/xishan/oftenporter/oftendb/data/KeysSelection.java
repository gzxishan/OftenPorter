package cn.xishan.oftenporter.oftendb.data;

/**
 * 键选择
 */
public class KeysSelection
{
    String[] keys;
    //boolean isSelect;


    public KeysSelection()
    {
        //select(isSelect);
    }

    /**
     * 设置键名。若使用的是@Key则会根据相关规则进行名称确定,即：若根据键名找到了对应的Field字段且该字段有@Key注解且value不为空，
     * 则键名替换为该value值。
     *
     * @param keys
     * @return
     */
    public KeysSelection names(String... keys)
    {
        this.keys = keys;
        return this;
    }

//    /**
//     * 是否选择
//     *
//     * @param isSelect
//     * @return
//     */
//    public KeysSelection select(boolean isSelect)
//    {
//        this.isSelect = isSelect;
//        return this;
//    }
}
