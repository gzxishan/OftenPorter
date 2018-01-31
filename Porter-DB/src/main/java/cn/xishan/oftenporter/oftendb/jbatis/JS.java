package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import javax.script.Invocable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/21.
 */
@AutoSetDefaultDealt(gen = JDaoGen.class)
public interface JS
{
    <T> T call(Object... args);

    <T> T callMethod(String method, Object... args);

    public static class Builder
    {
        private String script, injectScript;
        private Map<String, Object> interfaces;
        private String name;

        public Builder(String name, String script)
        {
            this.script = script;
            interfaces = new HashMap<>();
        }

        public String getName()
        {
            return name;
        }

        public Builder setInjectScript(String injectScript)
        {
            this.injectScript = injectScript;
            return this;
        }

        public Builder putInterface(String key, Object object)
        {
            interfaces.put(key, object);
            return this;
        }

        public JS build()
        {
            try
            {
                Invocable invocable = JDaoGen.getJsInvocable(script, injectScript, interfaces);
                JS js = new JsImpl(name, invocable);
                return js;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }

        }

    }

    static class JsImpl implements JS
    {
        Invocable invocable;
        String name;
        private static final Logger LOGGER = LogUtil.logger(JsImpl.class);

        public JsImpl(String name, Invocable invocable)
        {
            this.name = name;
            this.invocable = invocable;
        }

        private String getMethod()
        {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            return stacks[3].getMethodName();
        }

        @Override
        public <T> T call(Object... args)
        {
            return callMethod(getMethod(), args);
        }

        @Override
        public <T> T callMethod(String method, Object... args)
        {
            try
            {
                LOGGER.debug("js call:{}:{}", name, method);
                T t = (T) invocable.invokeFunction(method, args);
                return t;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
