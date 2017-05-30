package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.OKPacketEncoder;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.HandshakeResponse41Packet;
import com.mysqlproxy.mysql.protocol.OKPacket;
import com.mysqlproxy.mysql.protocol.ServerStatus;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.ComIdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/24.
 */
public class FrontendAuthenticatingStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendAuthenticatingStateHandler.class);
    public final static FrontendAuthenticatingStateHandler INSTANCE = new FrontendAuthenticatingStateHandler();

    private FrontendAuthenticatingStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object o) {
        if (o != null) {
            HandshakeResponse41Packet handshakeResponse41Packet = (HandshakeResponse41Packet) o;
            //TODO 暂时只检查用户名
            if (handshakeResponse41Packet.username.equals("root")) {
                logger.debug("前端认证成功");
                int packetLenth = 7;
                byte sequenceId = 2;
                OKPacket okPacket = new OKPacket(packetLenth, sequenceId);
                okPacket.header = 0;
                okPacket.lastInsertId = 0;
                okPacket.affectedRows = 0;
                okPacket.statusFlags |= ServerStatus.SERVER_STATUS_AUTOCOMMIT;
                okPacket.warnings = 0;
                try {
                    mysqlConnection.writePacket(okPacket, OKPacketEncoder.INSTANCE);
                } catch (IOException e) {
                    mysqlConnection.setState(CloseState.INSTANCE);
                    mysqlConnection.drive(null);
                }
            } else {
                logger.debug("前端认证失败");
            }
        } else {
            try {
                if(mysqlConnection.flushWriteBuffer()){
                    mysqlConnection.disableWriteAndEnableRead();
                    mysqlConnection.setState(ComIdleState.INSTANCE);
                }
            } catch (IOException e) {
                mysqlConnection.setState(CloseState.INSTANCE);
                mysqlConnection.drive(null);
            }
        }
    }
}
