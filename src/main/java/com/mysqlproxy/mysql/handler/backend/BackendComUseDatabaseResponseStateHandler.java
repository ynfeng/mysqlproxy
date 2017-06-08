package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendComUseDatabaseResponseStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendComUseDatabaseResponseStateHandler.class);

    public static final BackendComUseDatabaseResponseStateHandler INSTANCE = new BackendComUseDatabaseResponseStateHandler();

    private BackendComUseDatabaseResponseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        try {
            logger.debug("后端收到COM_USE响应");
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
            FrontendMysqlConnection frontendMysqlConnection = backendMysqlConnection.getFrontendMysqlConnection();
            MyByteBuff myByteBuff = connection.read();
            if (myByteBuff.getReadableBytes() >= 4) {
                int packetLength = (int) myByteBuff.getFixLenthInteger(0, 3);
                if (myByteBuff.getReadableBytes() >= packetLength + 4) {
                    //只处理完整包,不管是error还是ok，直接透传
                    backendMysqlConnection.disableRead();
                    backendMysqlConnection.getWriteBuffer().clear();
                    backendMysqlConnection.setState(ComIdleState.INSTANCE);

                    frontendMysqlConnection.setWriteBuff(myByteBuff);
                    frontendMysqlConnection.drive(null);
                }
            }
        } catch (Exception e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
