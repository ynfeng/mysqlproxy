package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.state.ComIdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendComQueryResponseResultSetRowStateHandler implements StateHandler<MyByteBuff> {
    private Logger logger = LoggerFactory.getLogger(BackendComQueryResponseColumnDefStateHandler.class);
    public static final BackendComQueryResponseResultSetRowStateHandler INSTANCE = new BackendComQueryResponseResultSetRowStateHandler();

    private BackendComQueryResponseResultSetRowStateHandler() {
    }

    @Override
    public void handle(MysqlConnection connection, MyByteBuff myByteBuff) {
        BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection) connection;
        FrontendMysqlConnection frontendMysqlConnection = backendMysqlConnection.getFrontendMysqlConnection();
        try {
            if (myByteBuff == null) {
                myByteBuff = connection.read();
            }
            for (; ; ) {
                int pos = backendMysqlConnection.getPacketScanPos();
                if (myByteBuff.getWriteIndex() > pos + 5) {
                    int marker = (int) myByteBuff.getFixLenthInteger(pos + 4, 1);
                    if (marker == 0xFE) {
                        logger.debug("后端ResultSetRow包结束标志，写到客户端，并进入空闲状态");
                        backendMysqlConnection.disableRead();
                        backendMysqlConnection.getWriteBuffer().clear();
                        backendMysqlConnection.setState(ComIdleState.INSTANCE);

                        frontendMysqlConnection.setWriteBuff(myByteBuff);
                        frontendMysqlConnection.drive(null);
                        break;
                    } else {
                        logger.debug("后端检查ResultSetRow包");
                        int resultSetRowPacketLen = (int) myByteBuff.getFixLenthInteger(pos, 3);
                        pos = pos
                                + 4 //包头
                                + resultSetRowPacketLen; //包内容长度
                        backendMysqlConnection.setPacketScanPos(pos);
                        if (myByteBuff.getWriteIndex() <= pos) {
                            //不是完整包，透传一次，等待下次读mysql数据时继续
                            logger.debug("后端ResultSetRow不是完整包，透传");
                            frontendMysqlConnection.setWriteBuff(myByteBuff);
                            frontendMysqlConnection.drive(null);
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO 处理异常
        }

    }
}
