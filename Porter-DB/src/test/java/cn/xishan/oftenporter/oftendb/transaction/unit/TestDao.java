package cn.xishan.oftenporter.oftendb.transaction.unit;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.oftendb.transaction.entity.TestEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2019/1/2.
 */
@MyBatisMapper(entityClass = TestEntity.class)
public interface TestDao
{
    void initTable();

    void insert(TestEntity testEntity);

    void deleteById(@Param("id") String id);

    void clearAll();

    boolean contains(@Param("id") String id);

}
