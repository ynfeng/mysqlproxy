package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComQueryResponseResultSetRowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BackendComQueryResponseColumnDefStateHandler implements StateHandler<MyByteBuff> {
    private Logger logger = LoggerFactory.getLogger(BackendComQueryResponseColumnDefStateHandler.class);

    public static final BackendComQueryResponseColumnDefStateHandler INSTANCE = new BackendComQueryResponseColumnDefStateHandler();

    private BackendComQueryResponseColumnDefStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {
        logger.debug("后端检查ColumnDefinition包");
        BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
        try {
            if (myByteBuff == null) {
                myByteBuff = connection.read();
            }
            int pos = backendMysqlConnection.getPacketScanPos();
            int columnDefPacketLen = (int) myByteBuff.getFixLenthInteger(pos, 3);
            int marker = (int) myByteBuff.getFixLenthInteger(pos + 4, 1);
            pos = pos
                    + 4 //包头
                    + columnDefPacketLen; //包内容长度
            backendMysqlConnection.setPacketScanPos(pos);
            if (marker == 0xFE) {
                logger.debug("后端ColumnDefinition包结束标志，进入行数据检查状态");
                backendMysqlConnection.setState(ComQueryResponseResultSetRowState.INSTANCE);
                backendMysqlConnection.drive(myByteBuff);
            } else {
                if (myByteBuff.getReadableBytes() >= pos) {
                    //是完整包，自驱一下
                    backendMysqlConnection.drive(myByteBuff);
                } else {
                    logger.debug("后端ColumnDefinition包未接收完全，透传");
                    //不是完整包，透传一次，等待下次读mysql数据时继续
                    FrontendMysqlConnection frontendMysqlConnection = backendMysqlConnection.getFrontendMysqlConnection();
                    frontendMysqlConnection.setWriteBuff(myByteBuff);
                    frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                    frontendMysqlConnection.drive(null);
                }
            }
        } catch (IOException e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
