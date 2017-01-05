package cn.xishan.oftenporter.porter.core.base;

/**
 * 用于检测。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface CheckPassable
{


//    /**
//     * 返回null表示通过。
//     *
//     * @param wObject 不同的检测时期，内部初始化情况不同，见{@linkplain DuringType}
//     * @param type    检测的时期
//     * @return 返回null表示通过，不为null（错误信息）表示无法通过。
//     */
//    Object willPass(WObject wObject, DuringType type,Aspect aspect);

    /**
     * 异步检测。
     *
     * @param wObject 不同的检测时期，内部初始化情况不同，见{@linkplain DuringType}
     * @param type    检测的时期
     */
    void willPass(WObject wObject, DuringType type,CheckHandle checkHandle);
}
