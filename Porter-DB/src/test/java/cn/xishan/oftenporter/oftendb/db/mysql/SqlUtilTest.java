package cn.xishan.oftenporter.oftendb.db.mysql;

import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
public class SqlUtilTest
{
    public static void main(String[] args){
        List<SqlUtil.CreateTable> list = SqlUtil.exportCreateTable("xs_global_user","jdbc:mysql://localhost:3306/xs_global?user=root&password=123456","com.mysql.jdbc.Driver");
        LogUtil.printErrPos(list);
    }
}
