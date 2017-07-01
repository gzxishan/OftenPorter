package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public class ABOption
{
    public final Object _otherObject;
    public final PortFunType portFunType;
    public final ABInvokeOrder abInvokeOrder;

    public ABOption(Object _otherObject, PortFunType portFunType, ABInvokeOrder abInvokeOrder)
    {
        this._otherObject = _otherObject;
        this.portFunType = portFunType;
        this.abInvokeOrder = abInvokeOrder;
    }


    public ABOption clone(ABInvokeOrder invokeOrder)
    {
        ABOption abOption = new ABOption(_otherObject, portFunType, invokeOrder);
        return abOption;
    }

    public boolean isFirst()
    {
        return abInvokeOrder == ABInvokeOrder.ORIGIN_FIRST || abInvokeOrder == ABInvokeOrder.BOTH_FIRST_LAST;
    }

    public boolean isLast()
    {
        return abInvokeOrder == ABInvokeOrder.FINAL_LAST || abInvokeOrder == ABInvokeOrder.BOTH_FIRST_LAST;
    }

}
