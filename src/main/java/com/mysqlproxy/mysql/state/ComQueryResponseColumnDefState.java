package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;

public class ComQueryResponseColumnDefState implements MysqlConnectionState{
    public static final ComQueryResponseColumnDefState INSTANCE = new ComQueryResponseColumnDefState();

    private ComQueryResponseColumnDefState(){}
    @Override
    public void backendHandle(MysqlConnection connection, Object t) {

    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object t) {

    }
}
