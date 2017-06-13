package com.mysqlproxy.mysql.handler.frontend;

import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.handler.StateHandler;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.FinalState;

public class FrontendCloseStateHandler implements StateHandler<MysqlPacket> {
    public static final FrontendCloseStateHandler INSTANCE = new FrontendCloseStateHandler();

    private FrontendCloseStateHandler() {

    }

    public void handle(MysqlConnection connection, MysqlPacket packet) {
        try {
            MysqlConnection mysqlConnection = (MysqlConnection) connection;
            mysqlConnection.close();
            mysqlConnection.setState(FinalState.INSTANCE);
            mysqlConnection.drive(null);
        } catch (Exception e) {
            //TODO 异常处理
            e.printStackTrace();
        }
    }
}
