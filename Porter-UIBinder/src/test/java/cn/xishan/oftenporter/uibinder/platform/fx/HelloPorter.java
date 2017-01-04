package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
@PortIn("Hello")
public class HelloPorter
{
    @PortIn(value = "ok", nece = {"name", "text", "checkbox", "toggle", "radio", "progress", "slider", "choice",
            "color", "date", "combox"})
    public void ok(WObject wObject)
    {
        LogUtil.printErrPosLn(
                wObject.fn[0] + ":" + wObject.fn[1] + ":checkbox=" + wObject.fn[2] + ":toggle=" + wObject.fn[3] +
                        ":radio=" + wObject.fn[4]
                        + ":progress=" + wObject.fn[5] + ":slider=" + wObject.fn[6] + ":choice=" + wObject.fn[7] +
                        ":color=" + wObject.fn[8] + ":date=" + wObject.fn[9] + ":combox=" + wObject.fn[10]);
    }

    @PortIn(value = "file")
    public void file(){
        LogUtil.printErrPosLn();
    }
    @PortIn(value = "fileClose")
    public void fileClose(){
        LogUtil.printErrPosLn();
    }

    @PortIn(value = "edit")
    public void edit(){
        LogUtil.printErrPosLn();
    }
}
