package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-06.
 */
public class _NeceUnece
{
    String varName;
    boolean isTrim;
    boolean clearBlank;
    String deleteRegex;

    public _NeceUnece()
    {
    }

    public _NeceUnece(String varName)
    {
        this.varName = varName;
    }


    public boolean isTrim()
    {
        return isTrim;
    }

    public boolean isClearBlank()
    {
        return clearBlank;
    }

    public String getDeleteRegex()
    {
        return deleteRegex;
    }

    void setDeleteRegex(String deleteRegex)
    {
        if (OftenTool.isEmpty(deleteRegex))
        {
            this.deleteRegex = null;
        } else
        {
            Pattern.compile(deleteRegex);
            this.deleteRegex = deleteRegex;
        }
    }

    public String getVarName()
    {
        return varName;
    }

    public Object dealString(Object v)
    {
        if (v instanceof CharSequence)
        {
            if (clearBlank)
            {
                v = String.valueOf(v).replaceAll("[\\s]", "");
            } else if (isTrim)
            {
                v = String.valueOf(v).trim();
            }

            if (deleteRegex != null)
            {
                v = String.valueOf(v).replaceAll(deleteRegex, "");
            }
        }
        return v;
    }

}
