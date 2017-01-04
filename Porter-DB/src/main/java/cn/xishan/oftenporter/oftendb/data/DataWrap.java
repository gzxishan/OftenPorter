package cn.xishan.oftenporter.oftendb.data;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 只对public字段有效。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/9.
 */
public class DataWrap extends DataDynamic
{
    /**
     * @param collectionName 集合或表的名称
     * @param clazz          获取字段名的注解与{@linkplain DataUtil#getTiedName(Field)}相同
     */
    public DataWrap(String collectionName, Class<?> clazz) throws RuntimeException
    {
        this(collectionName, getNameAndTieds(clazz));
    }

    private DataWrap(String collectionName, String[][] dbNameAndKeys) throws RuntimeException
    {
        super(collectionName, dbNameAndKeys[0], dbNameAndKeys[1]);
    }


    private static String[][] getNameAndTieds(Class<?> clazz)
    {

        List<String> tiedList = new ArrayList<>(), nameList = new ArrayList<>();
        try
        {
            Field[] fields = clazz.getFields();
            for (Field field : fields)
            {
                field.setAccessible(true);
                String name = DataUtil.getTiedName(field);
                if (name != null)
                {
                    tiedList.add(name);
                    nameList.add(field.getName());
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        String[][] nameAndTied = {
                nameList.toArray(new String[0]),
                tiedList.toArray(new String[0])
        };
        return nameAndTied;

    }
}
