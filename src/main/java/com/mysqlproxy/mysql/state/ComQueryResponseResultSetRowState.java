package com.mysqlproxy.mysql.state;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryResponseResultSetRowStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComQueryResponseResultSetRowStateHandler;

public class ComQueryResponseResultSetRowState implements MysqlConnectionState {
    public static ComQueryResponseResultSetRowState INSTANCE = new ComQueryResponseResultSetRowState();

    private ComQueryResponseResultSetRowState(){}

    @Override
    public void backendHandle(MysqlConnection connection, Object t) {
        BackendComQueryResponseResultSetRowStateHandler.INSTANCE.handle(connection,(MyByteBuff) t);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {
        FrontendComQueryResponseResultSetRowStateHandler.INSTANCE.handle(connection,(MyByteBuff) t);
    }
}
