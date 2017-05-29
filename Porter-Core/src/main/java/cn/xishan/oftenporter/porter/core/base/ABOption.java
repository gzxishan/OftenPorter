package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public class ABOption
{
    public final Object _otherObject;
    public final ABType abType;
    public final ABPortType abPortType;

    public ABOption(Object _otherObject, ABType abType, ABPortType abPortType)
    {
        this._otherObject = _otherObject;
        this.abType = abType;
        this.abPortType = abPortType;
    }

    /**
     * 如果{@linkplain #abPortType}为{@linkplain ABPortType#BOTH_FIRST_LAST ABPortType.BOTH_FIRST_LAST
     * }或{@linkplain ABPortType#ORIGIN_FIRST ABPortType.ORIGIN_FIRST}返回true。
     *
     * @return
     */
    public boolean isFirst()
    {
        return abPortType == ABPortType.BOTH_FIRST_LAST || abPortType == ABPortType.ORIGIN_FIRST;
    }

}
