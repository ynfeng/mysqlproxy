package com.mysqlproxy.mysql.handler;


import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.protocol.CommandType;
import com.mysqlproxy.mysql.state.ComQueryState;


public abstract class AbstractComIdleStateHandler implements StateHandler {

    protected void switchState(MysqlConnection connection, int readableBytes, byte commandType, int packageLength) {
        if (readableBytes >= 4) {
            if (commandType == CommandType.COM_QUERY) {
                connection.setDirectTransferPacketLen(packageLength + 4);
                connection.setState(ComQueryState.INSTANCE);
                connection.drive(null);
            }
        }
    }
}
