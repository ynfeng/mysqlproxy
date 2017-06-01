package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.RespondHandshakeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FrontendConnectingStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendConnectingStateHandler.class);
    public static final FrontendConnectingStateHandler INSTANCE = new FrontendConnectingStateHandler();

    private FrontendConnectingStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        logger.debug("写前端握手包");
        try {
            if (connection.flushWriteBuffer()) {
                connection.getWriteBuffer().clear();
                connection.disableWriteAndEnableRead();
                connection.setState(RespondHandshakeState.INSTANCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            connection.setState(CloseState.INSTANCE);
            connection.drive(null);
        }
    }
}
