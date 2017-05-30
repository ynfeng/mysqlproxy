package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.state.AuthenticatingState;
import com.mysqlproxy.mysql.state.CloseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/20.
 */
public class BackendResponseHandshakeStateHandler implements StateHandler<InitialHandshakeV10Packet> {
    private Logger logger = LoggerFactory.getLogger(BackendAuthenticatingStateHandler.class);
    public static final BackendResponseHandshakeStateHandler INSTANCE = new BackendResponseHandshakeStateHandler();

    private BackendResponseHandshakeStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, InitialHandshakeV10Packet packet) {
        logger.debug("后端发送握手包响应");
        try {
            if (mysqlConnection.flushWriteBuffer()) {
                mysqlConnection.getReadBuffer().clear();
                mysqlConnection.disableWriteAndEnableRead();
                mysqlConnection.setState(AuthenticatingState.INSTANCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
