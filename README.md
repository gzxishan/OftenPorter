# OftenPorter
简介:轻量级的url接口框架。

##
# 版本
当前最新版本为  [**1.2.119**](https://mvnrepository.com/artifact/com.xishankeji/Porter-Core)

![Version](https://img.shields.io/badge/Version-1.2.119-brightgreen.svg)
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
1. 增加`IdGen`.`randChars()`

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
