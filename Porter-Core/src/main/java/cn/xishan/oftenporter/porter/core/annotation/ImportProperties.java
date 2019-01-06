package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.*;
import java.util.Properties;

/**
 * 导入properties配置，另见:{@linkplain ResourceUtil}。
 * <p>
 * 支持替换参数:#{properName}。如app.lib=#{basedir}/lib。见{@linkplain IConfigData}。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018-10-29.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
@Importer(ImportProperties.Handle.class)
public @interface ImportProperties
{
    String[] value();

    /**
     * 手动输入键值对配置（优先级低于配置文件的）,每一个元素的格式：key=value
     *
     * @return
     */
    String[] keyValues() default {};

    class Handle implements Importer.Configable<ImportProperties>
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(Handle.class);

        @Override
        public void beforeCustomerConfig(PorterConf porterConf, ImportProperties importProperties) throws IOException
        {
            String[] paths = importProperties.value();
            IConfigData configData = porterConf.getConfigData();

            String[] keyValues = importProperties.keyValues();
            for (String kv : keyValues)
            {
                int index = kv.indexOf("=");
                if (index <= 0)
                {
                    throw new InitException("illegal keyValues:" + importProperties + ",kv=" + kv);
                }
                configData.set(kv.substring(0, index), kv.substring(index + 1));
            }

            for (String path : paths)
            {
                LOGGER.debug("load properties:{}", path);
                InputStream in = ResourceUtil.getAbsoluteResourceStream(path);
                Properties properties = new Properties();
                properties.load(in);
                configData.putAll(properties);
            }
        }
    }
}
