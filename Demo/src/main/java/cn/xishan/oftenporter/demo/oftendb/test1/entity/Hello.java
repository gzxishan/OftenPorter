package cn.xishan.oftenporter.demo.oftendb.test1.entity;

import java.util.Date;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class Hello
{
    private String id;
    private String name;
    private Date createtime;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getCreatetime()
    {
        return createtime;
    }

    public void setCreatetime(Date createtime)
    {
        this.createtime = createtime;
    }
}
