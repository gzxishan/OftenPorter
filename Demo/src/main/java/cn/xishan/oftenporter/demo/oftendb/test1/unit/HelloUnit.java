package cn.xishan.oftenporter.demo.oftendb.test1.unit;

import cn.xishan.oftenporter.demo.oftendb.test1.entity.Hello;
import cn.xishan.oftenporter.oftendb.annotation.TransactionDB;
import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.util.IdGen;

import java.util.Date;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */

public class HelloUnit
{
    @AutoSet
    IHelloDao helloDao;

    @TransactionDB
    public void initTable(){
        helloDao.initTable();
    }

    @TransactionDB(level = Isolation.DEFAULT)
    public void add(Hello hello)
    {
        hello.setCreatetime(new Date());
        hello.setId(IdGen.getDefault(2018,5).nextId());
        helloDao.add(hello);
    }

    @TransactionDB
    public void deleteById(String id)
    {
        helloDao.deleteById(id);
    }

    @TransactionDB
    public void updateName(String name, String newName)
    {
        helloDao.updateName(name, newName);
    }

    @TransactionDB
    public void deleteByName(String name)
    {
        helloDao.deleteByName(name);
    }

    @TransactionDB
    public void clearAll(){
        helloDao.clearAll();
    }

    @TransactionDB(setSavePoint = true)
    public void addHasSavePoint(Hello hello,boolean willFail)throws Exception
    {
        hello.setCreatetime(new Date());
        hello.setId(IdGen.getDefault(2018,5).nextId());
        helloDao.add(hello);
        if(willFail){
            throw new Exception("customer error!");
        }
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
