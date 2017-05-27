package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class ComIdleState implements MysqlConnectionState{
    public static final ComIdleState INSTANCE = new ComIdleState();


    @Override
    public void backendHandle(MysqlConnection connection, Object o) {
        System.out.println("处于查询模式");
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object o) {

    }
}
