package com.mysqlproxy.mysql.handler.backend;


import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.InitalHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.ConnectingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ynfeng on 2017/5/18.
 */
public class BackendInitialStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendAuthenticatingStateHandler.class);
    public static final BackendInitialStateHandler INSTANCE = new BackendInitialStateHandler();

    private BackendInitialStateHandler() {

    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object object) {
        try {
            logger.debug("后端接收Mysql初始握手包");
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
            e.printStackTrace();
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
