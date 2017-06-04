package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComQueryResponseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class BackendComQueryStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendComQueryStateHandler.class);

    public static final BackendComQueryStateHandler INSTANCE = new BackendComQueryStateHandler();

    private BackendComQueryStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
        MyByteBuff myByteBuff = backendMysqlConnection.getWriteBuffer();
        try {
            if (backendMysqlConnection.getDirectTransferPacketWriteLen() != 0 &&
                    backendMysqlConnection.isDirectTransferComplete()) {
                logger.debug("后端向MYSQL发送COM_QUERY包完成，转换至下一状态");
                backendMysqlConnection.setDirectTransferPacketWriteLen(0);
                backendMysqlConnection.setDirectTransferPacketLen(0);
                connection.disableWriteAndEnableRead();
                backendMysqlConnection.setState(ComQueryResponseState.INSTANCE);
                backendMysqlConnection.getFrontendMysqlConnection().drive(null);
            } else {
                logger.debug("后端向MYSQL发送COM_QUERY包");
                if(!backendMysqlConnection.canWrite()){
                    backendMysqlConnection.enableWrite();
                    return;
                }
                backendMysqlConnection.writeInDirectTransferMode(myByteBuff);
            }
        } catch (IOException e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
