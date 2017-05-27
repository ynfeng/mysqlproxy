package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.backend.BackendAuthenticatingStateHandler;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class AuthenticatingState implements MysqlConnectionState {
    public static final AuthenticatingState INSTANCE = new AuthenticatingState();


    @Override
    public void backendHandle(MysqlConnection connection, Object o) {
        BackendAuthenticatingStateHandler.INSTANCE.handle(connection,o);
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object o) {

    }
}
