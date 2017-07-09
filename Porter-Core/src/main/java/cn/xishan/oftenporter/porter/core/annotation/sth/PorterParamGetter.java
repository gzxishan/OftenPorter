package cn.xishan.oftenporter.porter.core.annotation.sth;

/**
 * Created by chenyg on 2017-05-09.
 */
public interface PorterParamGetter
{
     String getContext();
     String getClassTied();
     String getFunTied();
     void check();
}
