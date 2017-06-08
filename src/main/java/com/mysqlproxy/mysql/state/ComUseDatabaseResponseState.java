package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComUseDatabaseResponseStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComUseDatabaseResponseStateHandler;

public class ComUseDatabaseResponseState implements MysqlConnectionState {
    public static final ComUseDatabaseResponseState INSTANCE = new ComUseDatabaseResponseState();

    private ComUseDatabaseResponseState() {
    }

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComUseDatabaseResponseStateHandler.INSTANCE.handle(connection, t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComUseDatabaseResponseStateHandler.INSTANCE.handle(connection, t);
    }
}
