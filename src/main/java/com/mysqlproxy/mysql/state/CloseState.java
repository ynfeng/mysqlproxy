package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.protocol.MysqlPacket;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class CloseState implements MysqlConnectionState<MysqlPacket> {
    public static final CloseState INSTANCE = new CloseState();


    @Override
    public void backendHandle(MysqlConnection connection, MysqlPacket packet) {
        MysqlConnection mysqlConnection = (MysqlConnection) connection;
        mysqlConnection.close();
        mysqlConnection.setState(FinalState.INSTANCE);
        mysqlConnection.drive(null);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, MysqlPacket o) {

    }
}
