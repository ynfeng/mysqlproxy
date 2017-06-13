package com.mysqlproxy.mysql.state;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.frontend.FrontendCloseStateHandler;
import com.mysqlproxy.mysql.protocol.MysqlPacket;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class CloseState implements MysqlConnectionState {
    public static final CloseState INSTANCE = new CloseState();


    @Override
    public void backendHandle(MysqlConnection connection, Object object) {

    }

    @Override
    public void frontendHandle(MysqlConnection connection, Object object) {
        FrontendCloseStateHandler.INSTANCE.handle(connection, (MysqlPacket) object);
    }
}
