package com.mysqlproxy.mysql;

import com.mysqlproxy.mysql.state.MysqlConnectionState;

/**
 * Created by ynfeng on 2017/5/11.
 */
public interface StatefulConnection {
    void setState(MysqlConnectionState state);

    MysqlConnectionState getState();

    void drive(Object attachment);

}
