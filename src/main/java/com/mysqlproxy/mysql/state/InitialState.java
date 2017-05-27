package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendInitialStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontInitialStateHandler;

/**
 * Created by ynfeng on 2017/5/11.
 * 初始状态
 */
public class InitialState implements MysqlConnectionState {
    public static final InitialState INSTANCE = new InitialState();

    @Override
    public void backendHandle(MysqlConnection connection, Object o) {
        BackendInitialStateHandler.INSTANCE.handle(connection, o);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object o) {
        FrontInitialStateHandler.INSTANCE.handle(connection, o);
    }
}
