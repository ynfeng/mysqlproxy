package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComQueryResponseResultSetRowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FrontendComQueryResponseColumnDefStateHandler implements StateHandler<MyByteBuff> {
    private Logger logger = LoggerFactory.getLogger(FrontendComQueryResponseColumnDefStateHandler.class);

    public static final FrontendComQueryResponseColumnDefStateHandler INSTANCE = new FrontendComQueryResponseColumnDefStateHandler();

    private FrontendComQueryResponseColumnDefStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {
        logger.debug("前端检查ColumnDefinition包");
        FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
        BackendMysqlConnection backendMysqlConnection = frontendMysqlConnection.getBackendMysqlConnection();
        try {
            if (frontendMysqlConnection.getDirectTransferPacketWriteLen() != 0 &&
                    frontendMysqlConnection.isDirectTransferComplete()) {
                //部分包写完了，等待后端数据
                logger.debug("前端ColumnDefinition,部分包写完，等待后端数据");
                frontendMysqlConnection.disableWrite();
                frontendMysqlConnection.setDirectTransferPacketLen(0);
                frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
            } else {
                if(myByteBuff == null){
                    myByteBuff = frontendMysqlConnection.getWriteBuffer();
                }
                int pos = frontendMysqlConnection.getPacketScanPos();
                int columnDefPacketLen = (int) myByteBuff.getFixLenthInteger(pos, 3);
                int marker = (int) myByteBuff.getFixLenthInteger(pos + 4, 1);
                pos = pos
                        + 4 //包头
                        + columnDefPacketLen; //包内容长度
                frontendMysqlConnection.setPacketScanPos(pos);
                if (marker == 0xFE) {
                    logger.debug("前端ColumnDefinition包结束标志，进入行数据检查状态");
                    frontendMysqlConnection.setState(ComQueryResponseResultSetRowState.INSTANCE);
                    frontendMysqlConnection.drive(myByteBuff);
                } else {
                    if (myByteBuff.getReadableBytes() >= pos) {
                        //是完整包，自驱一下
                        frontendMysqlConnection.drive(myByteBuff);
                    } else {
                        logger.debug("前端ColumnDefinition包未接收完全，传到客户端");
                        if (!frontendMysqlConnection.isWriteMode()) {
                            frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                            frontendMysqlConnection.enableWrite();
                            return;
                        }
                        frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
                    }
                }
            }

        } catch (IOException e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
