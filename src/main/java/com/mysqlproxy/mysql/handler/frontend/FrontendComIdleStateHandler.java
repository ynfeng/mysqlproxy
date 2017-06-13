package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.Constants;
import com.mysqlproxy.ServerContext;
import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.BackendMysqlConnectionFactory;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.AbstractComIdleStateHandler;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.FinalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FrontendComIdleStateHandler extends AbstractComIdleStateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendComIdleStateHandler.class);

    public static final FrontendComIdleStateHandler INSTANCE = new FrontendComIdleStateHandler();

    private FrontendComIdleStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        try {
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
            BackendMysqlConnection backendMysqlConnection = frontendMysqlConnection.getBackendMysqlConnection();
            MyByteBuff myByteBuff = frontendMysqlConnection.read();
            int readableBytes = myByteBuff.getReadableBytes();
            if (readableBytes >= 5) {
                byte commandType = (byte) myByteBuff.getFixLenthInteger(4, 1);
                int packetLength = (int) myByteBuff.getFixLenthInteger(0, 3);
                if (backendMysqlConnection == null) {
                    //TODO 根据sql从后端连接池中取出连接中取出连接
                    //TODO 如果没有则新建
                    logger.debug("收到客户端命令,准备创建后端连接，或者从连接池中取出连接？？");
                    backendMysqlConnection = BackendMysqlConnectionFactory.INSTANCE.create(Constants.MYSQL_SERVER_IP, Constants.MYSQL_SERVER_PORT);
                    backendMysqlConnection.setFrontendMysqlConnection(frontendMysqlConnection);
                    frontendMysqlConnection.setBackendMysqlConnection(backendMysqlConnection);
                    ServerContext.getInstance().getConnector().connect(backendMysqlConnection);
                    return;
                }
                if (backendMysqlConnection != null &&
                        (backendMysqlConnection.getState() instanceof CloseState ||
                                backendMysqlConnection.getState() instanceof FinalState)) {
                    frontendMysqlConnection.setState(CloseState.INSTANCE);
                    frontendMysqlConnection.drive(o);
                    return;
                }

                switchState(connection, readableBytes, commandType, packetLength);
            }
        } catch (IOException e) {
            //TODO 异常处理
            e.printStackTrace();
            connection.disableRead();
            connection.setState(CloseState.INSTANCE);
            connection.drive(null);
        }

    }
}
