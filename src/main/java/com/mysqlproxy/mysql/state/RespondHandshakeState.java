package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendResponseHandshakeStateHandler;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;

/**
 * Created by ynfeng on 2017/5/11.
 *
 */
public class RespondHandshakeState implements MysqlConnectionState<InitialHandshakeV10Packet> {
    public static final RespondHandshakeState INSTANCE = new RespondHandshakeState();

    @Override
    public void backendHandle(MysqlConnection connection, InitialHandshakeV10Packet packet) {
        BackendResponseHandshakeStateHandler.INSTANCE.handle(connection,packet);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, InitialHandshakeV10Packet packet) {

    }
}
