package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;

public class BackendComQueryResponseColumnDefStateHandler implements StateHandler<MyByteBuff> {
    public static final BackendComQueryResponseColumnDefStateHandler INSTANCE = new BackendComQueryResponseColumnDefStateHandler();

    private BackendComQueryResponseColumnDefStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {

    }
}
