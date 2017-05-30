package com.mysqlproxy.mysql.handler.frontend;


import com.mysqlproxy.ServerContext;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.InitalHandshakeCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.handler.backend.BackendAuthenticatingStateHandler;
import com.mysqlproxy.mysql.protocol.CapabilityFlags;
import com.mysqlproxy.mysql.protocol.CharacterSet;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.protocol.ServerStatus;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.ConnectingState;
import com.mysqlproxy.util.AuthenticationMethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/18.
 */
public class FrontendInitialStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendInitialStateHandler.class);
    public static final FrontendInitialStateHandler INSTANCE = new FrontendInitialStateHandler();

    private FrontendInitialStateHandler() {

    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object object) {
        logger.debug("向前端响应初始握手包");
        //构建初始握手包
        String serverVersion = "mysqlproxy 0.0.1";
        String authPluginName = "mysql_native_password";
        int packageLent = 47 + serverVersion.length() + authPluginName.length();
        int capabilitys = 0;
        capabilitys |= CapabilityFlags.CLIENT_LONG_PASSWORD;
        capabilitys |= CapabilityFlags.CLIENT_FOUND_ROWS;
        capabilitys |= CapabilityFlags.CLIENT_LONG_FLAG;
        capabilitys |= CapabilityFlags.CLIENT_CONNECT_WITH_DB;
        capabilitys |= CapabilityFlags.CLIENT_NO_SCHEMA;
        capabilitys |= CapabilityFlags.CLIENT_COMPRESS;
        capabilitys |= CapabilityFlags.CLIENT_ODBC;
        capabilitys |= CapabilityFlags.CLIENT_LOCAL_FILES;
        capabilitys |= CapabilityFlags.CLIENT_IGNORE_SPACE;
        capabilitys |= CapabilityFlags.CLIENT_PROTOCOL_41;
        capabilitys |= CapabilityFlags.CLIENT_INTERACTIVE;
        capabilitys |= CapabilityFlags.CLIENT_IGNORE_SIGPIPE;
        capabilitys |= CapabilityFlags.CLIENT_TRANSACTIONS;
        capabilitys |= CapabilityFlags.CLIENT_RESERVED;
        capabilitys |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
        capabilitys |= CapabilityFlags.CLIENT_SECURE_CONNECTION;

        capabilitys |= CapabilityFlags.CLIENT_MULTI_STATEMENTS;
        capabilitys |= CapabilityFlags.CLIENT_MULTI_RESULTS;
        capabilitys |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
//        capabilitys |= CapabilityFlags.CLIENT_CONNECT_ATTRS;
        capabilitys |= CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
        capabilitys |= CapabilityFlags.CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS;
        capabilitys |= CapabilityFlags.UNUSED;

        InitialHandshakeV10Packet packet = new InitialHandshakeV10Packet(packageLent, (byte) 0);
        packet.protocolVersion = 0x0a;
        packet.serverVersion = serverVersion;
        packet.connectionId = ServerContext.getInstance().getIdGenerator().get();
        packet.authPluginDataPart1 = AuthenticationMethodUtil.randomString(8);
        packet.capabilityFlags = capabilitys;
        packet.characterSet = CharacterSet.latin1_swedish_ci;
        packet.statusFlag |= ServerStatus.SERVER_STATUS_AUTOCOMMIT;
        packet.lengthOfAuthPluginData = 21;
        packet.authPluginDataPart2 = AuthenticationMethodUtil.randomString(12);
        packet.authPluginName = authPluginName;
        try {
            mysqlConnection.disableRead();
            mysqlConnection.setState(ConnectingState.INSTANCE);
            mysqlConnection.writePacket(packet, InitalHandshakeCodec.INSTANCE);
        } catch (Exception e) {
            mysqlConnection.setState(CloseState.INSTANCE);
            mysqlConnection.drive(null);
        }
    }
}
