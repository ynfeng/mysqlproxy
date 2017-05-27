package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;

/**
 * Created by ynfeng on 2017/5/20.
 * <p>
 */
public class FrontendResponseHandshakeStateHandler implements StateHandler<InitialHandshakeV10Packet> {

    public static final FrontendResponseHandshakeStateHandler INSTANCE = new FrontendResponseHandshakeStateHandler();

    private FrontendResponseHandshakeStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, InitialHandshakeV10Packet packet) {

    }
}
