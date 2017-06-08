package com.mysqlproxy.mysql.state;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryResponseColumnDefStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComQueryResponseColumnDefStateHandler;

public class ComQueryResponseColumnDefState implements MysqlConnectionState {
    public static final ComQueryResponseColumnDefState INSTANCE = new ComQueryResponseColumnDefState();

    private ComQueryResponseColumnDefState() {
    }

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComQueryResponseColumnDefStateHandler.INSTANCE.handle(connection, (MyByteBuff) t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComQueryResponseColumnDefStateHandler.INSTANCE.handle(connection, (MyByteBuff) t);
    }
}
