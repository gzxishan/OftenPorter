package cn.xishan.oftenporter.oftendb.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2019/3/28.
 */
public class SimpleSqlUtilTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSqlUtilTest.class);
    @Test
    public void test1(){
        SimpleSqlUtil simpleSqlUtil = new SimpleSqlUtil();
        SimpleSqlUtil.SQLPart sqlPart = simpleSqlUtil.fromNameValues(null,null,"$or[","$true","$false","$or]");
        LOGGER.debug("where:{}",sqlPart.nowhere);
    }
}