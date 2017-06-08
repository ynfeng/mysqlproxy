package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryResponseColumnDefStateHandler;
import com.mysqlproxy.mysql.handler.backend.BackendComQueryResponseResultSetRowStateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendComQueryResponseResultSetRowStateHandler implements StateHandler<MyByteBuff> {
    private Logger logger = LoggerFactory.getLogger(BackendComQueryResponseColumnDefStateHandler.class);
    public static final FrontendComQueryResponseResultSetRowStateHandler INSTANCE = new FrontendComQueryResponseResultSetRowStateHandler();

    private FrontendComQueryResponseResultSetRowStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {
        try {
            logger.debug("前端检查ResultSetRow包");
            FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection)frontendMysqlConnection.getBackendMysqlConnection();
            if (frontendMysqlConnection.getDirectTransferPacketWriteLen() != 0 &&
                    frontendMysqlConnection.isDirectTransferComplete()) {
                if (backendMysqlConnection.getState() instanceof ComIdleState) {
                    logger.debug("前端ResultSetRow,响应完毕,进入空闲状态");
                    logger.debug("释放后端连接？？?");
                    frontendMysqlConnection.getReadBuffer().clear();
                    frontendMysqlConnection.getWriteBuffer().clear();
                    frontendMysqlConnection.setState(ComIdleState.INSTANCE);
                    frontendMysqlConnection.setDirectTransferPacketLen(0);
                    frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
                    frontendMysqlConnection.setPacketScanPos(0);
                    frontendMysqlConnection.disableWriteAndEnableRead();
                } else {
                    //部分包写完了，等待后端数据
                    logger.debug("前端ResultSetRow,部分包写完，等待后端数据");
                    frontendMysqlConnection.disableWrite();
                    frontendMysqlConnection.setDirectTransferPacketLen(0);
                    frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
                }
            } else {
                if(myByteBuff == null){
                    myByteBuff = frontendMysqlConnection.getWriteBuffer();
                }
                int pos = frontendMysqlConnection.getPacketScanPos();
                int ResultSetRowPacketLen = (int) myByteBuff.getFixLenthInteger(pos, 3);
                int marker = (int) myByteBuff.getFixLenthInteger(pos + 4, 1);
                if (marker == 0xFE) {
                    logger.debug("前端ResultSetRow包结束标志，专到客户端，并进入空闲状态");
                    if (!frontendMysqlConnection.isWriteMode()) {
                        frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                        frontendMysqlConnection.enableWrite();
                        return;
                    }
                    frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
                } else {
                    pos = pos
                            + 4 //包头
                            + ResultSetRowPacketLen; //包内容长度
                    frontendMysqlConnection.setPacketScanPos(pos);
                    if (myByteBuff.getReadableBytes() >= pos) {
                        //依然是完整包
                        frontendMysqlConnection.drive(myByteBuff);
                    } else {
                        //不是完整包，透传一次，等待后端数据
                        if (!frontendMysqlConnection.isWriteMode()) {
                            frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                            frontendMysqlConnection.enableWrite();
                            return;
                        }
                        logger.debug("前端ResultSetRow不是完整包，传到客户端");
                        frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO 处理异常
        }

    }
}
