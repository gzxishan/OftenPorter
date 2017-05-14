package cn.xishan.oftenporter.oftendb.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 键选择
 */
public class KeysSelection
{
  private List<String> keys;
    //boolean isSelect;


    public KeysSelection()
    {
        //select(isSelect);
        keys = new ArrayList<>();
    }

    public String[] getKeys()
    {
        return keys.toArray(new  String[0]);
    }

    /**
     * 添加键名。若使用的是@Key则会根据相关规则进行名称确定,即：若根据键名找到了对应的Field字段且该字段有@Key注解且value不为空，
     * 则键名替换为该value值。
     *
     * @param keys
     * @return
     */
    public KeysSelection names(String... keys)
    {
        for (int i = 0; i < keys.length; i++)
        {
            this.keys.add(keys[i]);
        }
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
