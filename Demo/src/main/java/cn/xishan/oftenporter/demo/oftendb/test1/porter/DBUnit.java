package cn.xishan.oftenporter.demo.oftendb.test1.porter;

import cn.xishan.oftenporter.oftendb.data.common;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.jbatis.JDao;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * Created by chenyg on 2017-04-29.
 */
public class DBUnit
{
    @AutoSet
    DBSource dbSource;

    @AutoSet
    JDao jDao;


    public Object add(WObject wObject)
    {
        JSONObject add = new JSONObject();
        add.put("_id", KeyUtil.randomUUID());
        add.put("name", "jdao");
        add.put("time", new Date());
        add.put("sex", "男");
        add.put("age", 12);
        AdvancedExecutor executor = jDao.execute(add, wObject);
        return common.advancedExecute(wObject, dbSource, executor);
    }



}
