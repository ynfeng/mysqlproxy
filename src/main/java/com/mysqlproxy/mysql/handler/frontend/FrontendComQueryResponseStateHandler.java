package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.ErrorPacketEncoder;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComQueryResponseColumnDefState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FrontendComQueryResponseStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(FrontendComQueryResponseStateHandler.class);

    public static final FrontendComQueryResponseStateHandler INSTANCE = new FrontendComQueryResponseStateHandler();

    private FrontendComQueryResponseStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, Object o) {
        FrontendMysqlConnection frontendMysqlConnection = (FrontendMysqlConnection) connection;
        BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) ((FrontendMysqlConnection) connection).getBackendMysqlConnection();
        try {
            if (frontendMysqlConnection.getDirectTransferPacketWriteLen() != 0 &&
                    frontendMysqlConnection.isDirectTransferComplete()) {
                if (backendMysqlConnection.getState() instanceof ComIdleState) {
                    logger.debug("前端COM_QUERY_RESPONSE,响应ERROR包,进入空闲状态");
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
                    logger.debug("前端COM_QUERY_RESPONSE,部分包写完，等待后端数据");
                    frontendMysqlConnection.disableWrite();
                    frontendMysqlConnection.setDirectTransferPacketLen(0);
                    frontendMysqlConnection.setDirectTransferPacketWriteLen(0);
                }
            } else {
                MyByteBuff myByteBuff = connection.getWriteBuffer();
                if (myByteBuff.getReadableBytes() >= 5) {
                    int marker = (int) myByteBuff.getFixLenthInteger(4, 1);
                    int packetLen = (int) myByteBuff.getFixLenthInteger(0, 3);
                    if (marker == 0xFF || marker == 0 || marker == 0xFE) {
                        //error包，直接传给前端，
                        if (!frontendMysqlConnection.isWriteMode()) {
                            frontendMysqlConnection.setDirectTransferPacketLen(packetLen);
                            frontendMysqlConnection.enableWrite();
                            return;
                        }
                        logger.debug("前端连接向客户端发送COM_QUERY_RESPONSE,error包");
                        frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
                    } else {
                        //驱动状态机
                        int fieldCountPacketLen = (int) myByteBuff.getFixLenthInteger(0, 3);
                        if (myByteBuff.getReadableBytes() >= fieldCountPacketLen + 4) {
                            //第一个包完整，进入下一状态
                            connection.setPacketScanPos(fieldCountPacketLen + 4);
                            connection.setState(ComQueryResponseColumnDefState.INSTANCE);
                            connection.drive(myByteBuff);
                        } else {
                            logger.debug("前端连接向客户端发送COM_QUERY_RESPONSE，不是完整包");
                            //不是完整包，发送至前端
                            if (!frontendMysqlConnection.isWriteMode()) {
                                frontendMysqlConnection.enableWrite();
                                frontendMysqlConnection.setDirectTransferPacketLen(myByteBuff.getReadableBytes());
                                return;
                            }
                            frontendMysqlConnection.writeInDirectTransferMode(myByteBuff);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
