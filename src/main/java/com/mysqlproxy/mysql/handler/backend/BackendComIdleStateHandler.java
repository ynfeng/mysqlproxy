package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.AbstractComIdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BackendComIdleStateHandler extends AbstractComIdleStateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendComIdleStateHandler.class);
    public static final BackendComIdleStateHandler INSTANCE = new BackendComIdleStateHandler();

    private BackendComIdleStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
        MyByteBuff myByteBuff = backendMysqlConnection.getWriteBuffer();
        int readableBytes = myByteBuff.getReadableBytes();
        if (readableBytes >= 4) {
            byte commandType = (byte) myByteBuff.getFixLenthInteger(4, 1);
            int packetLength = (int) myByteBuff.getFixLenthInteger(0, 3);
            switchState(connection, readableBytes, commandType, packetLength);
        }
    }
}
