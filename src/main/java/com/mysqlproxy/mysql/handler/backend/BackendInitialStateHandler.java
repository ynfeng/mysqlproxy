package com.mysqlproxy.mysql.handler.backend;


import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.MysqlBackendConnectionPool;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.InitalHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.ConnectingState;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/18.
 */
public class BackendInitialStateHandler implements StateHandler {
    public static final BackendInitialStateHandler INSTANCE = new BackendInitialStateHandler();

    private BackendInitialStateHandler() {

    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object object) {
        try {
            MysqlPacket packet = mysqlConnection.readPacket(InitalHandshakeCodec.INSTANCE);
            if (packet instanceof ErrorPacket) {
                mysqlConnection.disableRead();
                mysqlConnection.setState(CloseState.INSTANCE);
                mysqlConnection.drive(null);
            } else if (packet instanceof InitialHandshakeV10Packet) {
                mysqlConnection.disableRead();
                mysqlConnection.setState(ConnectingState.INSTANCE);
                mysqlConnection.drive(packet);
            } else {
                mysqlConnection.disableRead();
                mysqlConnection.setState(CloseState.INSTANCE);
                mysqlConnection.drive(null);
            }
        } catch (Exception e) {
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
