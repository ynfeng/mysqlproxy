package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComUseDatabaseResponseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendComUseDatabaseStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendComUseDatabaseStateHandler.class);

    public static final BackendComUseDatabaseStateHandler INSTANCE = new BackendComUseDatabaseStateHandler();

    private BackendComUseDatabaseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object obj) {
        try {
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) backendMysqlConnection.getFrontendMysqlConnection();
            if (backendMysqlConnection.getDirectTransferPacketWriteLen() != 0
                    && backendMysqlConnection.isDirectTransferComplete()) {
                logger.debug("后端COM_USE命令发送完成，转换至COM_USE响应状态");
                backendMysqlConnection.getReadBuffer().clear();
                backendMysqlConnection.getWriteBuffer().clear();
                backendMysqlConnection.disableWrite();
                backendMysqlConnection.setState(ComUseDatabaseResponseState.INSTANCE);
                backendMysqlConnection.setDirectTransferPacketLen(0);
                backendMysqlConnection.setDirectTransferPacketWriteLen(0);
                backendMysqlConnection.disableWriteAndEnableRead();
                frontendMysqlConnection.drive(null);
            } else {
                if (!backendMysqlConnection.isWriteMode()) {
                    backendMysqlConnection.enableWrite();
                    return;
                }
                logger.debug("后端准备向mysql发送COM_USE命令");
                MyByteBuff myByteBuff = backendMysqlConnection.getWriteBuffer();
                backendMysqlConnection.writeInDirectTransferMode(myByteBuff);
            }
        } catch (Exception e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
