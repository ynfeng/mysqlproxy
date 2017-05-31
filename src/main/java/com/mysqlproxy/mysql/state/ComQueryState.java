package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComIdleStateHandler;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComQueryStateHandler;

public class ComQueryState implements MysqlConnectionState {
    public static final ComQueryState INSTANCE = new ComQueryState();

    private ComQueryState() {
    }

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComQueryStateHandler.INSTANCE.handle(connection,t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComQueryStateHandler.INSTANCE.handle(connection, t);
    }
}
