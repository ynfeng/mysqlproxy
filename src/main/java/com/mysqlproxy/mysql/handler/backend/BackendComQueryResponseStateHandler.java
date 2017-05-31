package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
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
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) ((BackendMysqlConnection) connection).getFrontendMysqlConnection();

            if (backendMysqlConnection.getDirectTransferPacketWriteLen() != 0 &&
                    backendMysqlConnection.isDirectTransferComplete()) {

            } else {
                logger.info("后端收到COM_QUERY_RESPONSE包,准备向前端透传");
                MyByteBuff myByteBuff = backendMysqlConnection.read();
                if(backendMysqlConnection.getDirectTransferPacketWriteLen() == 0){
                    frontendMysqlConnection.recyleWriteBuffer();
                    frontendMysqlConnection.setWriteBuff(myByteBuff);
                }
                backendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                frontendMysqlConnection.drive(null);
            }
        } catch (IOException e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
