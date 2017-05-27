package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;

/**
 * Created by ynfeng on 2017/5/11.
 */
public interface MysqlConnectionState<T> {
    void backendHandle(MysqlConnection connection, T t);

    void frontendHandle(MysqlConnection connection, T t);
}
