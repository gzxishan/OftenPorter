package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.porter.core.JResponse;

public interface SimpleDealt
{
   void deal(JResponse jResponse, Object... objects)throws Exception;
   void onException(Exception e, JResponse jResponse, Object... objects);
}
