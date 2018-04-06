package cn.xishan.oftenporter.porter.core.base;


import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

/**
 * <pre>
 *     执行的顺序：
 *     ON_GLOBAL---ON_CONTEXT---BEFORE_CLASS--ON_CLASS---BEFORE_METHOD---ON_METHOD---AFTER_METHOD
 *                                                                                |___ON_METHOD_EXCEPTION
 *     <ul>
 *         <li>{@linkplain #ON_GLOBAL}:只对{@linkplain CommonMain#addGlobalCheck(CheckPassable)}有效</li>
 *         <li>{@linkplain #ON_CONTEXT}:只对{@linkplain PorterConf#addContextCheck(CheckPassable)}有效</li>
 *     </ul>
 * </pre>
 * <p>
 * 另见:{@linkplain PorterConf#addContextCheck(CheckPassable)},{@linkplain PorterConf#addPorterCheck(CheckPassable)}
 * {@linkplain PortIn#checks()},{@linkplain PortIn#checksForWholeClass()}
 * </p>
 *
 * <strong>注意：调用顺序</strong>，假如按顺序添加了如下检测器:C1,C2,...,Cx，则调用顺序如下：
 * <ol>
 * <li>顺序调用[C1,C2,...,Cx]:
 * {@linkplain #ON_GLOBAL}--&gt;{@linkplain #ON_CONTEXT}--&gt;
 * {@linkplain #BEFORE_CLASS}--&gt;{@linkplain #ON_CLASS}--&gt;
 * {@linkplain #BEFORE_METHOD}--&gt;{@linkplain #ON_METHOD}
 * </li>
 * <li>逆序调用[Cx,...,C2,C1]:
 * {@linkplain #AFTER_METHOD}或{@linkplain #ON_METHOD_EXCEPTION}
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public enum DuringType
{
    /**
     * 没有初始化任何参数。通过{@linkplain CommonMain#addGlobalCheck(CheckPassable) CommonMain.addGlobalCheck(CheckPassable)}添加，
     * 且在第一次{@linkplain CommonMain#startOne(PorterConf) CommonMain.startOne(PorterConf)}之前有效。
     */
    ON_GLOBAL,

    /**
     * 没有初始化任何参数。
     */
    ON_CONTEXT,

    /**
     * 此时类参数没有准备好。
     */
    BEFORE_CLASS,

    /**
     * 此时类参数已经准备完成。
     */
    ON_CLASS,

    /**
     * 在执行接口函数前，此时函数参数未准备好。
     */
    BEFORE_METHOD,

    /**
     * 在执行接口函数前，此时函数参数已经准备完成。
     */
    ON_METHOD,

    /**
     * 在执行接口函数后,
     * {@linkplain CheckPassable#willPass(WObject, DuringType, CheckHandle)}的{@linkplain CheckHandle#returnObj}为返回值。
     * <br>
     * 使用的检测接口同{@linkplain #ON_METHOD}
     */
    AFTER_METHOD,
    /**
     * 在执行接口函数抛出了异常时,
     * {@linkplain CheckPassable#willPass(WObject, DuringType, CheckHandle)}的{@linkplain CheckHandle#exCause}为异常信息。
     * <br>
     * 此时如果{@linkplain CheckHandle#go(Object)}返回的对象为null,则输出异常信息。
     * <br>
     * 使用的检测接口同{@linkplain #ON_METHOD}
     */
    ON_METHOD_EXCEPTION
}
