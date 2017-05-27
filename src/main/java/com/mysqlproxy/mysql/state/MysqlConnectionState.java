package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;

/**
 * Created by ynfeng on 2017/5/11.
 */
public interface MysqlConnectionState {
    void backendHandle(MysqlConnection connection, Object t);

    void frontendHandle(MysqlConnection connection, Object t);
}
