package cn.xishan.oftenporter.porter.core.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import cn.xishan.oftenporter.porter.core.base.InNames.Name;

/**
 * 非线程安全。
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/11.
 */
public class BackableSeek
{


    /**
     * <pre>
     *     Add:表示把name-typeId添加到{@linkplain BackableSeek}中。
     *     Bind:表示找出{@linkplain InNames}中某个{@linkplain Name#typeParserId}为null的,
     *     并将其typeParserId设置成{@linkplain BackableSeek}中与该{@linkplain Name#varName}对应的typeParserId(如果存在).
     * </pre>
     */
    public enum SeekType
    {
        Add_Bind,
        Add_NotBind,
        NotAdd_Bind,
        NotAdd_NotBind
    }

    private Stack<Map<String, String>> stack = new Stack<>();

    /**
     * push一个map
     */
    public void push()
    {
        Map<String, String> map = new HashMap<>();
        stack.push(map);
    }

    /**
     * pop一个map
     */
    public void pop()
    {
        stack.pop();
    }

    public void put(String varName, String typeId)
    {
        stack.peek().put(varName, typeId);
    }

    /**
     * 会依此从栈顶map开始找，并返回第一个非空的值。
     *
     * @param varName 变量名
     * @return
     */
    public String getTypeId(String varName)
    {
        for (int i = stack.size() - 1; i >= 0; i--)
        {
            String typeId = stack.get(i).get(varName);
            if (typeId != null)
            {
                return typeId;
            }
        }
        return null;
    }

    /**
     * 返回的Iterator内部是：从栈顶开始，依此输出内容。
     *
     * @return
     */
    public Iterator<Map.Entry<String, String>> iterator()
    {
        Iterator<Map.Entry<String, String>> iterator = new Iterator<Map.Entry<String, String>>()
        {
            int index = stack.size() - 1;
            Iterator<Map.Entry<String, String>> current = stack.get(index).entrySet().iterator();

            @Override
            public boolean hasNext()
            {
                boolean has = current.hasNext();
                if (!has && index > 0)
                {
                    index--;
                    current = stack.get(index).entrySet().iterator();
                    has = current.hasNext();
                }
                return has;
            }

            @Override
            public Map.Entry<String, String> next()
            {
                return current.next();
            }

            @Override
            public void remove()
            {
                current.remove();
            }
        };
        return iterator;
    }

    /**
     * 找出{@linkplain Name#typeParserId}为null的，并设置成本对象中存储的对应的typeParserId.
     *
     * @param inNames
     */
    public void bindTypeId2NameNull(InNames inNames)
    {
        Name[] names = inNames.nece;
        for (int i = 0; i < names.length; i++)
        {
            if (names[i].typeParserId == null)
            {
                names[i].typeParserId = getTypeId(names[i].varName);
            }
        }
        names = inNames.unece;
        for (int i = 0; i < names.length; i++)
        {
            if (names[i].typeParserId == null)
            {
                names[i].typeParserId = getTypeId(names[i].varName);
            }
        }
    }

    /**
     * 把varName和typeId进行绑定。
     *
     * @param inNames
     * @param varName
     * @param typeId
     */
    public static void bindVarNameWithTypeId(InNames inNames, String varName, String typeId)
    {
        Name[] names = inNames.nece;
        for (int i = 0; i < names.length; i++)
        {
            if (names[i].typeParserId == null && names[i].varName.equals(varName))
            {
                names[i].typeParserId = typeId;
                return;
            }
        }
        names = inNames.unece;
        for (int i = 0; i < names.length; i++)
        {
            if (names[i].typeParserId == null && names[i].varName.equals(varName))
            {
                names[i].typeParserId = typeId;
                return;
            }
        }
    }

}
