package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.ResponseHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.HandshakeResponse41Packet;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.AuthenticatingState;
import com.mysqlproxy.mysql.state.CloseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ynfeng on 2017/5/20.
 * <p>
 */
public class FrontendResponseHandshakeStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendResponseHandshakeStateHandler.class);
    public static final FrontendResponseHandshakeStateHandler INSTANCE = new FrontendResponseHandshakeStateHandler();

    private FrontendResponseHandshakeStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object object) {
        try {
            logger.debug("接收前端握手包响应");
            MysqlPacket mysqlPacket = mysqlConnection.readPacket(ResponseHandshakeCodec.INSTANCE);
            if (mysqlPacket != null) {
                if (mysqlPacket instanceof HandshakeResponse41Packet) {
                    mysqlConnection.disableRead();
                    mysqlConnection.setState(AuthenticatingState.INSTANCE);
                    mysqlConnection.drive(mysqlPacket);
                }
            }
        } catch (Exception e) {
            mysqlConnection.disableRead();
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
