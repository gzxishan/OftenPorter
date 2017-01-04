package cn.xishan.oftenporter.porter.core.pbridge;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class PPath
{
    public final int step;
    public final PName pName;
    public final PLinker pLinker;

    public PPath(int step, PName pName, PLinker init)
    {
        this.step = step;
        this.pName = pName;
        this.pLinker = init;
    }

    public PPath newPath(int newStep)
    {
        return new PPath(newStep, pName, pLinker);
    }

    @Override
    public String toString()
    {
        return step + ":" + pName;
    }
}
