package com.mysqlproxy.mysql.handler.backend;

import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.codec.AuthenticateCodec;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.protocol.OKPacket;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.ComIdleState;
import com.mysqlproxy.mysql.state.ComQueryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ynfeng on 2017/5/24.
 */
public class BackendAuthenticatingStateHandler implements StateHandler {
    private Logger logger = LoggerFactory.getLogger(BackendAuthenticatingStateHandler.class);
    public final static BackendAuthenticatingStateHandler INSTANCE = new BackendAuthenticatingStateHandler();

    private BackendAuthenticatingStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object o) {
        try {
            MysqlPacket mysqlPacket = mysqlConnection.readPacket(AuthenticateCodec.INSTANCE);
            BackendMysqlConnection backendMysqlConnection = (BackendMysqlConnection)mysqlConnection;
            FrontendMysqlConnection frontendMysqlConnection = backendMysqlConnection.getFrontendMysqlConnection();
            if (mysqlPacket instanceof ErrorPacket) {
                logger.debug("后端接收Mysql认证响应结果,Error包");
                backendMysqlConnection.disableRead();
                backendMysqlConnection.setState(CloseState.INSTANCE);
                backendMysqlConnection.drive(null);

                frontendMysqlConnection.drive(mysqlPacket);
            } else if (mysqlPacket instanceof OKPacket) {
                logger.debug("后端接收Mysql认证响应结果,OK包");
                //认证成功，清空所有缓冲区
                backendMysqlConnection.getReadBuffer().clear();
                backendMysqlConnection.getWriteBuffer().clear();
                //转换到空闲状态
                backendMysqlConnection.setState(ComIdleState.INSTANCE);
                if (frontendMysqlConnection != null && (frontendMysqlConnection.getState() instanceof ComIdleState)) {
                    frontendMysqlConnection.drive(frontendMysqlConnection.getReadBuffer());
                }
            } else {
                backendMysqlConnection.disableRead();
                backendMysqlConnection.setState(CloseState.INSTANCE);
                backendMysqlConnection.drive(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mysqlConnection.setState(CloseState.INSTANCE);
        }

    }
}
