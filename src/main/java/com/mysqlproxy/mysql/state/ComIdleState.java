package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendComIdleStateHandler;
import com.mysqlproxy.mysql.handler.frontend.FrontendComIdleStateHandler;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class ComIdleState implements MysqlConnectionState {
    public static final ComIdleState INSTANCE = new ComIdleState();


    @Override
    public void backendHandle(MysqlConnection connection, Object o) {
        BackendComIdleStateHandler.INSTANCE.handle(connection, o);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object o) {
        FrontendComIdleStateHandler.INSTANCE.handle(connection, o);
    }
}
