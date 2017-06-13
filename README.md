# mysqlproxy

* 支持基本的crud
* 支持use xxx

# 调试方法
* 修改 `com.mysqlproxy.Constants` 类里的各种参数。
* 运行com.mysqlproxy.ServerBootstrap启动服务器。
* 使用mysql client或JDBC连接
* 部分mysql客户端可能不支持，因为有些命令没有实现
* debug级别的日志对性能有很大影响，性能测试关闭debug日志
* 仅供技术研究使用


