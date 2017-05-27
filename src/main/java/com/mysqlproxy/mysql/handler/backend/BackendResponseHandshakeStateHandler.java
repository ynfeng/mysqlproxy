package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.ResponseHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.CapabilityFlags;
import com.mysqlproxy.mysql.protocol.CharacterSet;
import com.mysqlproxy.mysql.protocol.HandshakeResponse41Packet;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.state.AuthenticatingState;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.util.AuthenticationMethodUtil;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/20.
 * <p>
 * 此状态下有两种处理:
 * 一种是生成握手响应,另一种是发送响应包
 * 如果状态机驱动方法的参数为null则表示正在发送响应
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
