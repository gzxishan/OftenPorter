package cn.xishan.oftenporter.porter.core.base;

/**
 * Created by cheyg on 2017/1/5.
 */
public abstract class CheckHandle {
    public Object returnObj;
    public Throwable exCause;
    /**
     * 如果失败的对象是{@linkplain cn.xishan.oftenporter.porter.core.JResponse}，则直接输出该结果。
     * @param failedObject 当此对象为空时表示成功，否则表示失败。
     */
    public abstract void go(Object failedObject);

    public void failed(Object failedObject) {
        go(failedObject);
    }

    public void next() {
        go(null);
    }
}
