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

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/24.
 */
public class BackendAuthenticatingStateHandler implements StateHandler {
    public final static BackendAuthenticatingStateHandler INSTANCE = new BackendAuthenticatingStateHandler();

    private BackendAuthenticatingStateHandler() {
    }

    @Override
    public void handle(MysqlConnection mysqlConnection, Object o) {
        try {
            MysqlPacket mysqlPacket = mysqlConnection.readPacket(AuthenticateCodec.INSTANCE);
            if (mysqlPacket instanceof ErrorPacket) {
                mysqlConnection.disableRead();
                mysqlConnection.setState(CloseState.INSTANCE);
                mysqlConnection.drive(null);
            } else if (mysqlPacket instanceof OKPacket) {
                //认证成功，清空所有缓冲区
                mysqlConnection.getReadBuffer().clear();
                mysqlConnection.getWriteBuffer().clear();
                FrontendMysqlConnection frontendMysqlConnection = ((BackendMysqlConnection) mysqlConnection).getFrontendMysqlConnection();
                if (frontendMysqlConnection != null) {
                    //此时的前端处于等待状态需要驱动到下一个状态
                    frontendMysqlConnection.drive(null);
                }
                //转换到空闲状态
                mysqlConnection.setState(ComIdleState.INSTANCE);
                mysqlConnection.enableRead();
            } else {
                //TODO 还有一个切换认证方法的请求未实现
                mysqlConnection.disableRead();
                mysqlConnection.setState(CloseState.INSTANCE);
                mysqlConnection.drive(null);
            }
        } catch (Exception e) {
            mysqlConnection.setState(CloseState.INSTANCE);
        }

    }
}
