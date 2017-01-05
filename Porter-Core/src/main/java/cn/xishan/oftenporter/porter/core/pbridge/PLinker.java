package cn.xishan.oftenporter.porter.core.pbridge;

import cn.xishan.oftenporter.porter.core.PorterAttr;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public interface PLinker extends Delivery
{
    public enum Direction
    {
        /**
         * 可以双向访问
         */
        Both,
        /**
         * 只是被添加者访问我。
         */
        ToMe,
        /**
         * 只是我访问被添加者。
         */
        ToIt,
        /**
         * 可以访问被添加者所访问的
         */
        ToItAll,
        /**
         * 可以访问我所访问的
         */
        ToMeAll,
        /**
         * 彼此可以访问彼此可访问的。
         */
        BothAll
    }

    public interface LinkListener
    {
        /**
         * 得到一个被添加者可达的路径。如果当前实例需要访问it，则需要该监听。
         *
         * @param pPath
         */
        void onItCanGo(PLinker it, PPath pPath);

    }



    /**
     * 调用者会把自己可达的路径发给自己返回的listener中。
     *
     * @return
     */
    LinkListener sendLink();

    /**
     * 把自己可达的路径发给该listener。
     *
     * @param init
     * @param linkListener 如果为null，表示移除监听；不为null，则表示设置监听。
     */
    void receiveLink(PLinker init, LinkListener linkListener);

    /**
     * 得到链接的PInit。
     * @param pName
     * @return
     */
    PLinker getLinkedPInit(String pName);


    void setPorterAttr(PorterAttr porterAttr);
    PorterAttr getPorterAttr();

    /**
     * 连接两个框架实例。
     *
     * @param it        被添加的框架实例（it）
     * @param direction 可访问的方向.
     */
    void link(PLinker it, Direction direction);

    /**
     * 关闭,会断开所有链接。
     */
    void close();

    /**
     * 是否已经关闭。
     *
     * @return
     */
    boolean isClosed();

    /**
     * 当匹配不到pname时，将会使用此处设置的对象来访问。
     */
    void setForAnyOtherPName(PLinker anyOther);

    /**
     * 如果返回true，则会被设置到对应的{@linkplain #setForAnyOtherPName(PLinker)}
     * @return
     */
    boolean isForAnyOtherPName();

}
