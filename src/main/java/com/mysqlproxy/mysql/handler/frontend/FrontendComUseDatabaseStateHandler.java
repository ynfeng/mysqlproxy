package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComUseDatabaseResponseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COM_USE命令以后可能切换服务器虚拟出的schema，目前阶段直接透传至mysql中
 */
public class FrontendComUseDatabaseStateHandler implements StateHandler<MyByteBuff> {
    private Logger logger = LoggerFactory.getLogger(FrontendComUseDatabaseStateHandler.class);

    public static final FrontendComUseDatabaseStateHandler INSTANCE = new FrontendComUseDatabaseStateHandler();

    private FrontendComUseDatabaseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {
        try {
            logger.debug("前端收到COM_USE命令");
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
            BackendMysqlConnection backendMysqlConnection = frontendMysqlConnection.getBackendMysqlConnection();

            if (frontendMysqlConnection.getDirectTransferPacketWriteLen() != 0
                    && frontendMysqlConnection.isDirectTransferComplete()) {
                logger.debug("前端COM_USE命令透传完成，转换至COM_USE响应状态");
                frontendMysqlConnection.getReadBuffer().clear();
                frontendMysqlConnection.getWriteBuffer().clear();
                frontendMysqlConnection.setState(ComUseDatabaseResponseState.INSTANCE);
                frontendMysqlConnection.setDirectTransferPacketLen(0);
                frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
            } else {
                logger.debug("前端COM_USE命令,准备透传");
                backendMysqlConnection.setWriteBuff(frontendMysqlConnection.getReadBuffer());
                backendMysqlConnection.drive(null);
            }
        } catch (Exception e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
