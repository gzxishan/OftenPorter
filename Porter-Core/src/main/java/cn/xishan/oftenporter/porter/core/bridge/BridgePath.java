package cn.xishan.oftenporter.porter.core.bridge;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class BridgePath
{
    public final int step;
    public final BridgeName bridgeName;
    public final BridgeLinker bridgeLinker;

    public BridgePath(int step, BridgeName bridgeName, BridgeLinker init)
    {
        this.step = step;
        this.bridgeName = bridgeName;
        this.bridgeLinker = init;
    }

    public BridgePath newPath(int newStep)
    {
        return new BridgePath(newStep, bridgeName, bridgeLinker);
    }

    @Override
    public String toString()
    {
        return step + ":" + bridgeName;
    }
}
