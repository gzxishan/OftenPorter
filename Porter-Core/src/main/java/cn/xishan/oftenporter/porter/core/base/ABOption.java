package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public class ABOption
{
    public final Object _otherObject;
    public final ABType abType;
    public final int total;

    /**
     * 从1开始。
     */
    public final int currentIndex;

    public ABOption(Object _otherObject, ABType abType, int total, int currentIndex)
    {
        this._otherObject = _otherObject;
        this.abType = abType;
        this.total = total;
        this.currentIndex = currentIndex;
    }

}
