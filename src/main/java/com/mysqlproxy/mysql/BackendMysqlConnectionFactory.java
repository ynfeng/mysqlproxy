package com.mysqlproxy.mysql;


/**
 * Created by ynfeng on 2017/5/12.
 */
public class BackendMysqlConnectionFactory {
    public static final BackendMysqlConnectionFactory INSTANCE = new BackendMysqlConnectionFactory();

    private BackendMysqlConnectionFactory() {
    }

    public BackendMysqlConnection create(String ip, int port) {
        BackendMysqlConnection mysqlConnection = new BackendMysqlConnection();
        mysqlConnection.setServerIp(ip);
        mysqlConnection.setServerPort(port);
        return mysqlConnection;
    }
}
