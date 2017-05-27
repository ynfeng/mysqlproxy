package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class FinalState implements MysqlConnectionState {
    public static final FinalState INSTANCE = new FinalState();


    @Override
    public void backendHandle(MysqlConnection connection, Object o) {
        System.out.println("已到达最终状态，连接已关闭");
    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object o) {
        System.out.println("已到达最终状态，连接已关闭");
    }
}
