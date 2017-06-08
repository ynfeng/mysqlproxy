package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComQueryResponseColumnDefState;
import com.mysqlproxy.mysql.state.ComQueryResponseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BackendComQueryResponseStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendComQueryResponseStateHandler.class);

    public static final BackendComQueryResponseStateHandler INSTANCE = new BackendComQueryResponseStateHandler();

    private BackendComQueryResponseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        try {
            logger.info("后端收到COM_QUERY_RESPONSE包");
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) backendMysqlConnection.getFrontendMysqlConnection();
            MyByteBuff myByteBuff = backendMysqlConnection.read();
            if (myByteBuff.getReadableBytes() >= 5) {
                int marker = (int) myByteBuff.getFixLenthInteger(4, 1);
                if (marker == 0xFF || marker == 0 || marker == 0xFE) {
                    //error包或者ok包
                    int errorPacketLen = (int) myByteBuff.getFixLenthInteger(0, 3);
                    if (myByteBuff.getReadableBytes() >= errorPacketLen + 4) {
                        //包并不大，为了实现简单，如果未收完整，等待接收完整后透传
                        backendMysqlConnection.disableRead();
                        backendMysqlConnection.getWriteBuffer().clear();
                        backendMysqlConnection.setState(ComIdleState.INSTANCE);

                        frontendMysqlConnection.setWriteBuff(myByteBuff);
                        frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                        frontendMysqlConnection.drive(null);
                    }
                } else {
                    int fieldCountPacketLen = (int) myByteBuff.getFixLenthInteger(0, 3);
                    if (myByteBuff.getReadableBytes() >= fieldCountPacketLen + 4) {
                        //第一个包完整，进入下一状态
                        connection.setPacketScanPos(fieldCountPacketLen + 4);
                        connection.setState(ComQueryResponseColumnDefState.INSTANCE);
                        connection.drive(myByteBuff);
                    }
                }
            }
        } catch (IOException e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
