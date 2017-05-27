package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.protocol.MysqlPacket;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class CloseState implements MysqlConnectionState {
    public static final CloseState INSTANCE = new CloseState();


    @Override
    public void backendHandle(MysqlConnection connection, Object object) {
        MysqlConnection mysqlConnection = (MysqlConnection) connection;
        mysqlConnection.close();
        mysqlConnection.setState(FinalState.INSTANCE);
        mysqlConnection.drive(null);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object object) {

    }
}
