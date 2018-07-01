package cn.xishan.oftenporter.demo.oftendb.test1.unit;

import cn.xishan.oftenporter.demo.oftendb.test1.entity.Hello;
import cn.xishan.oftenporter.oftendb.annotation.TransactionJDBC;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */

public class HelloUnit
{
    @AutoSet
    IHelloDao helloDao;

    @TransactionJDBC
    public void initTable(){
        helloDao.initTable();
    }

    @TransactionJDBC
    public void add(Hello hello)
    {
        helloDao.add(hello);
    }

    @TransactionJDBC
    public void deleteById(String id)
    {
        helloDao.deleteById(id);
    }

    @TransactionJDBC
    public void updateName(String name, String newName)
    {
        helloDao.updateName(name, newName);
    }

    @TransactionJDBC
    public void deleteByName(String name)
    {
        helloDao.deleteByName(name);
    }

    @TransactionJDBC
    public void clearAll(){
        helloDao.clearAll();
    }

    public List<Hello> listAll()
    {
        return helloDao.listAll();
    }

    public int count(String name)
    {
        return helloDao.count(name);
    }


}
