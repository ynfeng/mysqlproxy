package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.AbstractComIdleStateHandler;
import com.mysqlproxy.mysql.state.CloseState;
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
            MyByteBuff myByteBuff = frontendMysqlConnection.read();
            int readableBytes = myByteBuff.getReadableBytes();
            if (readableBytes >= 4) {
                byte commandType = (byte) myByteBuff.getFixLenthInteger(4, 1);
                int packetLength = (int) myByteBuff.getFixLenthInteger(0, 3);
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
