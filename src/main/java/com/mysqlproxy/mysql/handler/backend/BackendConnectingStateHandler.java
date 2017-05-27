package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.mysql.*;
import com.mysqlproxy.mysql.codec.InitalHandshakeCodec;
import com.mysqlproxy.mysql.codec.ResponseHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.*;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.RespondHandshakeState;
import com.mysqlproxy.util.AuthenticationMethodUtil;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/16.
 */
public class BackendConnectingStateHandler implements StateHandler<InitialHandshakeV10Packet> {

    public static final BackendConnectingStateHandler INSTANCE = new BackendConnectingStateHandler();


    @Override
    public void handle(MysqlConnection mysqlConnection, InitialHandshakeV10Packet packet) {
        //构建响应包
        String username = "root";
        byte[] passwrod = AuthenticationMethodUtil.generateMysqlNativePassword("123456", packet.authPluginDataPart);
        int packageLength = 35 + username.length() + passwrod.length + packet.authPluginName.length();
        byte sequenceId = 1;
        HandshakeResponse41Packet responseHandshake = new HandshakeResponse41Packet(packageLength, sequenceId);

        int capability = 0;
        capability |= CapabilityFlags.CLIENT_LONG_PASSWORD;
        capability |= CapabilityFlags.CLIENT_LONG_FLAG;
        capability |= CapabilityFlags.CLIENT_PROTOCOL_41;
        capability |= CapabilityFlags.CLIENT_INTERACTIVE;
        capability |= CapabilityFlags.CLIENT_TRANSACTIONS;
        capability |= CapabilityFlags.CLIENT_SECURE_CONNECTION;
        capability |= CapabilityFlags.CLIENT_MULTI_STATEMENTS;
        capability |= CapabilityFlags.CLIENT_MULTI_RESULTS;
        capability |= CapabilityFlags.CLIENT_PS_MULTI_RESULTS;
        capability |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
        capability |= CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
        capability |= CapabilityFlags.CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS;
        capability |= CapabilityFlags.CLIENT_SESSION_TRACK;
        capability |= CapabilityFlags.CLIENT_DEPRECATE_EOF;

        responseHandshake.capability = capability;
        responseHandshake.maxPacketSize = 16777216;
        responseHandshake.characterSet = CharacterSet.utf8_general_ci;
        responseHandshake.username = username;
        responseHandshake.authData = passwrod;
        responseHandshake.authPluginName = packet.authPluginName;
        try {
            mysqlConnection.setState(RespondHandshakeState.INSTANCE);
            mysqlConnection.writePacket(responseHandshake, ResponseHandshakeCodec.INSTANCE);
        } catch (IOException e) {
            // 编码出错，转换状态
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
