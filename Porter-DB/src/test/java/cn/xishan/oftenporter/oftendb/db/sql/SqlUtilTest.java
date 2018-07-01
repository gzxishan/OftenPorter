package cn.xishan.oftenporter.oftendb.db.sql;

import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
public class SqlUtilTest
{
    public static void main(String[] args){
        List<SqlUtil.CreateTable> list = SqlUtil.exportCreateTable("xs_global_user","jdbc:sql://localhost:3306/xs_global?user=root&password=123456","com.sql.jdbc.Driver");
        LogUtil.printErrPos(list);
    }
}
