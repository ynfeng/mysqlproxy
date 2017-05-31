package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class FrontendComQueryStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendComQueryStateHandler.class);

    public static final FrontendComQueryStateHandler INSTANCE = new FrontendComQueryStateHandler();

    private FrontendComQueryStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
        try {
            MyByteBuff myByteBuff = frontendMysqlConnection.read();
            if (frontendMysqlConnection.isDirectTransferComplete()) {
                //透传完成，转换状态
                logger.debug("前端COM_QUERY透传完成，转换至下一状态");
                if (myByteBuff.getReadableBytes() == 0) {
                    myByteBuff.clear();
                }
                //TODO 状态转换
                frontendMysqlConnection.setDirectTransferPacketLen(0);
                frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
                frontendMysqlConnection.disableRead();
            } else {
                logger.debug("前端接收COM_QUERY命令");
                //回收掉后端的写缓冲区，准备与前端共享
                BackendMysqlConnection backendMysqlConnection = frontendMysqlConnection.getBackendMysqlConnection();
                backendMysqlConnection.recyleWriteBuffer();
                backendMysqlConnection.setWriteBuff(myByteBuff);
                backendMysqlConnection.drive(null);
            }
        } catch (IOException e) {
            //TODO 处理异常
            e.printStackTrace();
        }
    }
}
