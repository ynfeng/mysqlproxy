package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.RespondHandshakeState;



public class FrontendConnectingStateHandler implements StateHandler {
    public static final FrontendConnectingStateHandler INSTANCE = new FrontendConnectingStateHandler();

    private FrontendConnectingStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        try {
            if (connection.flushWriteBuffer()) {
                connection.getWriteBuffer().clear();
                connection.disableWriteAndEnableRead();
                connection.setState(RespondHandshakeState.INSTANCE);
            }
        } catch (Exception e) {
            connection.setState(CloseState.INSTANCE);
            connection.drive(null);
        }
    }
}
