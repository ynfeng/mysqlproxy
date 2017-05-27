package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.state.AuthenticatingState;
import com.mysqlproxy.mysql.state.CloseState;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/20.
 */
public class BackendResponseHandshakeStateHandler implements StateHandler<InitialHandshakeV10Packet> {

    public static final BackendResponseHandshakeStateHandler INSTANCE = new BackendResponseHandshakeStateHandler();

    private BackendResponseHandshakeStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, InitialHandshakeV10Packet packet) {
        try {
            if (mysqlConnection.flushWriteBuffer()) {
                mysqlConnection.getReadBuffer().clear();
                mysqlConnection.disableWriteAndEnableRead();
                mysqlConnection.setState(AuthenticatingState.INSTANCE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
