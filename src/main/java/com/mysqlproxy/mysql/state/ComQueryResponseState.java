package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryResponseStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComQueryResponseStateHandler;

public class ComQueryResponseState implements MysqlConnectionState {
    public static final ComQueryResponseState INSTANCE = new ComQueryResponseState();

    private ComQueryResponseState() {
    }

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComQueryResponseStateHandler.INSTANCE.handle(connection, t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComQueryResponseStateHandler.INSTANCE.handle(connection, t);
    }
}
