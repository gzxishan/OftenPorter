package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.util.KeyUtil;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class XSServletWSConfig implements ServerApplicationConfig
{

    static final String WS_PATH = getPath();

    private static String getPath()
    {
        try
        {
            return "/" + KeyUtil.secureRandomKeySha256(128);
        } catch (Exception e)
        {
            return "/" + KeyUtil.random48Key() + KeyUtil.random48Key();
        }
    }

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> set)
    {
        Set<ServerEndpointConfig> sets = new HashSet<>();
        ServerEndpointConfig config = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, WS_PATH)
                .configurator(new HttpSessionConfigurator())
                .build();
        sets.add(config);
        return sets;
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> set)
    {
        return set;
    }
}
