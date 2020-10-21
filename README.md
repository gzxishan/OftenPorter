# OftenPorter
简介:轻量级的url接口框架。

##
# 版本
当前最新版本为  [**1.2.131**](https://mvnrepository.com/artifact/com.xishankeji/Porter-Core)

![Version](https://img.shields.io/badge/Version-1.2.131-brightgreen.svg)
![License](http://img.shields.io/:License-Apache2.0-blue.svg)
![JDK 1.8](https://img.shields.io/badge/JDK-1.8-green.svg)

# 码云
[https://gitee.com/xishankeji/OftenPorter](https://gitee.com/xishankeji/OftenPorter)
# github
[https://github.com/gzxishan/OftenPorter](https://github.com/gzxishan/OftenPorter)

##
# 文档(Documentation)
[Wiki](https://github.com/gzxishan/OftenPorter/wiki)

##
# 公司(Company)
[贵州溪山科技有限公司](http://www.xishankeji.com)

## 发布记录
### v进行中
1. 增加`TypeTo`.`parseParameter`；
2. 增加`TYPE`注解；
3. 完善`OftenTool`.`getCause`，支持`InvocationTargetException`异常的处理；

### v1.2.131 2020/10/07
1. 完善`SqlUtil`；
2. 完善`IdGen`；
3. 增加`ServletUtils`；

### v1.2.128 2020/09/14
1. 增加`IdGen`.`randChars()`；
2. 修复`queryArrayContains`判断问题；
3. 修复tomcat7启动报空指针的问题；
4. 修复日期转换问题，支持`/`分隔符；
5. 修复日期转换问题，小时问题修复；
6. 完善`WebSocket`会话的处理，用于支持自定义的会话存储场景；

### v1.2.119 2020/08/21
1. 完善`@PathMapping`，被注解的函数可以返回false、从而不自行处理请求；
2. 完善`MyBatisDao`，支持从Dao里获取对应的`MyBatisDao`，从而可以获取数据表名、配置；
3. 支持`MyBatis`的`Cursor`；
4. 完善工具类`HttpUtil`网络超时时间的设置；
5. 加入`getRequestUrlWithQuery`；

### v1.2.113 2020/07/10
1. 完善参数处理(`@Nece`与`@Unece`)，加入去除空白符选项；
2. 完善proxy工具类；
3. 升级fastjson版本；

### v1.2.109 2020/06/24
1. 完善Porter-Bridge-Servlet跨域处理；
2. 修复TableOption.dealQueryInnerValues的问题；
3. 完善数据源的切换；

### v1.2.98 2020/05/18
1. 完善`Htmlx`：增加file与filePattern属性；
2. 完善`OftenInitializer`，加入beforeStartOneForAll(PorterConf porterConf)；
3. 完善`Property`，加入choice:JsonPrefix,ArrayPrefix；
4. 增加PathMapping；
