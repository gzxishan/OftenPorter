package cn.xishan.oftenporter.servlet.websocket;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;

/**
 * @author Created by https://github.com/CLovinr on 2018/11/4.
 */
public class WebSocketOption
{
    private String[] subprotocols;

    private Class<? extends Decoder>[] decoders;

    private Class<? extends Encoder>[] encoders;

    private Extension[] extensions;


    public String[] getSubprotocols()
    {
        return subprotocols;
    }

    public void setSubprotocols(String[] subprotocols)
    {
        this.subprotocols = subprotocols;
    }

    public Class<? extends Decoder>[] getDecoders()
    {
        return decoders;
    }

    public void setDecoders(Class<? extends Decoder>[] decoders)
    {
        this.decoders = decoders;
    }

    public Class<? extends Encoder>[] getEncoders()
    {
        return encoders;
    }

    public void setEncoders(Class<? extends Encoder>[] encoders)
    {
        this.encoders = encoders;
    }

    public Extension[] getExtensions()
    {
        return extensions;
    }

    public void setExtensions(Extension[] extensions)
    {
        this.extensions = extensions;
    }
}
