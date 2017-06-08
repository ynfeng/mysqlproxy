package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendComUseDatabaseResponseStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendComUseDatabaseResponseStateHandler.class);

    public static final FrontendComUseDatabaseResponseStateHandler INSTANCE = new FrontendComUseDatabaseResponseStateHandler();

    private FrontendComUseDatabaseResponseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        try {
            logger.debug("前端发送COM_USE响应");
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
            if (frontendMysqlConnection.getDirectTransferPacketWriteLen() != 0
                    && frontendMysqlConnection.isDirectTransferComplete()) {
                frontendMysqlConnection.getWriteBuffer().clear();
                frontendMysqlConnection.disableWriteAndEnableRead();
                frontendMysqlConnection.setState(ComIdleState.INSTANCE);
            } else {
                MyByteBuff myByteBuff = frontendMysqlConnection.getWriteBuffer();
                if (!frontendMysqlConnection.isWriteMode()) {
                    frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                    frontendMysqlConnection.enableWrite();
                    return;
                }
                frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
            }
        } catch (Exception e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
