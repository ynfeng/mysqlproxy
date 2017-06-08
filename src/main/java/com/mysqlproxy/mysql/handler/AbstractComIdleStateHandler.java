package com.mysqlproxy.mysql.handler;


import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.ErrorPacketEncoder;
import com.mysqlproxy.mysql.protocol.CommandType;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComQueryState;
import com.mysqlproxy.mysql.state.ComUseDatabaseState;

import java.io.IOException;


public abstract class AbstractComIdleStateHandler implements StateHandler {

    protected void switchState(MysqlConnection connection, int readableBytes, byte commandType, int packageLength) {
        if (readableBytes >= 5) {
            if (commandType == CommandType.COM_QUERY) {
                connection.setDirectTransferPacketWriteLen(0);
                connection.setDirectTransferPacketLen(packageLength + 4);
                connection.setState(ComQueryState.INSTANCE);
                connection.drive(connection.getReadBuffer());
            }  else if(commandType == CommandType.COM_INIT_DB){
                connection.setDirectTransferPacketWriteLen(0);
                connection.setDirectTransferPacketLen(packageLength + 4);
                connection.setState(ComUseDatabaseState.INSTANCE);
                connection.drive(connection.getReadBuffer());
            }
            else {
                responseError(connection,"What's the fuck? This command seems not implements yet!");
            }
        }
    }

    private void responseError(MysqlConnection connection,String msg){
        try {
            if(connection.isWriteMode()){
                if(connection.flushWriteBuffer()){
                    connection.getWriteBuffer().clear();
                    connection.getReadBuffer().clear();
                    connection.disableWriteAndEnableRead();
                    connection.setState(ComIdleState.INSTANCE);
                    return;
                }
            }
            int packetLength = 9 + msg.length();
            byte sequenceId = 1;
            ErrorPacket errorPacket = new ErrorPacket(packetLength,sequenceId);
            errorPacket.header = (byte) 0xFF;
            errorPacket.errCode = 1064;
            errorPacket.sqlStateMarker = "#";
            errorPacket.sqlState = "42000";
            errorPacket.errMsg = msg;
            connection.disableRead();
            connection.writePacket(errorPacket, ErrorPacketEncoder.INSTANCE);
        } catch (IOException e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
