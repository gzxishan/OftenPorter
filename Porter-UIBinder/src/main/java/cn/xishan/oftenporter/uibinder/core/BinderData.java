package cn.xishan.oftenporter.uibinder.core;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhuiFeng on 2015/6/12.
 */
public class BinderData
{

    public class Task
    {
        public Object data;
        public AttrEnum method;

        public Task(AttrEnum method, Object data)
        {
            this.method = method;
            this.data = data;
        }
    }

    class GetTask
    {
        public BinderGetListener binderGetListener;
        public List<BinderGet> binderGets;

        public GetTask(BinderGetListener binderGetListener, List<BinderGet> binderGets)
        {
            this.binderGetListener = binderGetListener;
            this.binderGets = binderGets;
        }
    }

    private ArrayList<Task> list = new ArrayList<>(1);

    public BinderData()
    {

    }


    /**
     * @param method
     * @param data
     * @return 返回自己
     */
    private BinderData addTask(AttrEnum method, Object data)
    {
        list.add(new Task(method, data));
        return this;
    }

    /**
     * 用于获取值
     *
     * @param binderGetListener
     * @param binderGets
     * @return
     */
    public List<BinderGet> addGetTask(BinderGetListener binderGetListener, BinderGet... binderGets)
    {
        if (binderGetListener == null)
        {
            throw new NullPointerException();
        }
        List<BinderGet> list = new ArrayList<>();
        for (BinderGet binderGet : binderGets)
        {
            list.add(binderGet);
        }

        GetTask getTask = new GetTask(binderGetListener, list);
        addTask(AttrEnum.METHOD_ASYNC_GET, getTask);

        return list;
    }

    public int size()
    {
        return list.size();
    }

    public List<Task> getTasks()
    {
        return list;
    }


    /**
     * 添加设置任务.
     *
     * @param binderSets
     * @return 返回的对象可以用于继续添加
     */
    public List<BinderSet> addSetTask(BinderSet... binderSets)
    {
        List<BinderSet> list = new ArrayList<>();
        for (BinderSet binderSet : binderSets)
        {
            list.add(binderSet);
        }

        addTask(AttrEnum.METHOD_ASYNC_SET, list);
        return list;
    }

    public JResponse toResponse()
    {
        JResponse jResponse = new JResponse();
        jResponse.setCode(ResultCode.SUCCESS);
        jResponse.setResult(this);
        return jResponse;
    }

}
