package com.mysqlproxy.mysql.handler;

import com.mysqlproxy.mysql.MysqlConnection;

/**
 * Created by ynfeng on 2017/5/15.
 */
public interface StateHandler<T> {
    void handle(MysqlConnection connection, T t);
}
