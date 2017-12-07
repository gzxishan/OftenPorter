package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.annotation.FederatedMysqlOption;
import cn.xishan.oftenporter.oftendb.annotation.FederatedOption;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;

/**
 * 用于跨数据库连接.当使用{@linkplain FederatedOption}或{@linkplain FederatedMysqlOption}时,同时必须有{@linkplain DBSource}注入.
 *
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
@AutoSetDefaultDealt(gen = FederatedGen.class)
public interface Federated
{
    void init(DBSource dbSource, boolean dropTableIfExists, String tableName, String jdbcUrl, String driverClass,
            String connectionUrl);

    void initOfMysql(DBSource dbSource, boolean dropTableIfExists, String tableName, String host, String dbname,
            String user, String password);
}
