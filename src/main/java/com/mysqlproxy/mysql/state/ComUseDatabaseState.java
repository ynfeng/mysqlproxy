package com.mysqlproxy.mysql.state;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComUseDatabaseStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComUseDatabaseStateHandler;

public class ComUseDatabaseState implements MysqlConnectionState {
    public static final ComUseDatabaseState INSTANCE = new ComUseDatabaseState();

    private ComUseDatabaseState() {
    }

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComUseDatabaseStateHandler.INSTANCE.handle(connection, t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComUseDatabaseStateHandler.INSTANCE.handle(connection, (MyByteBuff) t);
    }
}
