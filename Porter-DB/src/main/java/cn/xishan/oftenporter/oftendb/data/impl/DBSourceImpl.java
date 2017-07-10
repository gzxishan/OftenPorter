package cn.xishan.oftenporter.oftendb.data.impl;


import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.DBHandle;


/**
 * @author ZhuiFeng
 */
public abstract class DBSourceImpl implements DBSource, Cloneable
{
    private ConfigToDo configToDo;
    private Configed configed;

    public DBSourceImpl(ConfigToDo configToDo)
    {
        this.configToDo = configToDo;
        configed = new ConfigedImpl();
    }

    @Override
    public Configed getConfiged()
    {
        return configed;
    }

    @Override
    public ConfigToDo getConfigToDo()
    {
        return configToDo;
    }


    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }


    @Override
    public DBSource newInstance()
    {

        try
        {
            DBSourceImpl dbSource = (DBSourceImpl) clone();
            dbSource.configToDo = this.configToDo;
            dbSource.configed = new ConfigedImpl();
            return dbSource;

        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
