package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendResponseHandshakeStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendResponseHandshakeStateHandler;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;

/**
 * Created by ynfeng on 2017/5/11.
 */
public class RespondHandshakeState implements MysqlConnectionState {
    public static final RespondHandshakeState INSTANCE = new RespondHandshakeState();

    @Override
    public void backendHandle(MysqlConnection connection, Object object) {
        BackendResponseHandshakeStateHandler.INSTANCE.handle(connection, (InitialHandshakeV10Packet) object);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object object) {
        FrontendResponseHandshakeStateHandler.INSTANCE.handle(connection, (InitialHandshakeV10Packet) object);
    }
}
