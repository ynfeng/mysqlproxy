package com.mysqlproxy.mysql;

import java.nio.channels.SocketChannel;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class FrontendMysqlConnectionFactory {
    public static final FrontendMysqlConnectionFactory INSTANCE = new FrontendMysqlConnectionFactory();

    private FrontendMysqlConnectionFactory() {
    }

    public FrontendMysqlConnection create(SocketChannel socketChannel, BackendMysqlConnection backendMysqlConnection) {
        FrontendMysqlConnection frontendMysqlConnection =  new FrontendMysqlConnection();
        frontendMysqlConnection.setSocketChannel(socketChannel);
        frontendMysqlConnection.setBackendMysqlConnection(backendMysqlConnection);
        return frontendMysqlConnection;
    }
}
