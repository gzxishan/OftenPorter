package cn.xishan.oftenporter.demo.oftendb.test1.unit;

import cn.xishan.oftenporter.demo.oftendb.test1.entity.Hello;
import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
@MyBatisMapper(entityClass = Hello.class)
public interface IHelloDao
{
    void initTable();

    void add(Hello hello);

    void deleteById(@Param("id") String id);

    void deleteByName(@Param("name")String name);

    List<Hello> listAll();

    int count(@Param("name") String name);

    void updateName(@Param("name")String name,@Param("newName")String newName);

    void clearAll();

}
