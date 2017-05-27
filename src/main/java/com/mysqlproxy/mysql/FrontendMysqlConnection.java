package com.mysqlproxy.mysql;

import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.InitialState;


/**
 * Created by ynfeng on 2017/5/11.
 */
public final class FrontendMysqlConnection extends MysqlConnection<MysqlPacket> {
    private BackendMysqlConnection backendMysqlConnection;


    public FrontendMysqlConnection() {
        setState(InitialState.INSTANCE);
    }

    public void close() {
    }

    public BackendMysqlConnection getBackendMysqlConnection() {
        return backendMysqlConnection;
    }

    public void setBackendMysqlConnection(BackendMysqlConnection backendMysqlConnection) {
        this.backendMysqlConnection = backendMysqlConnection;
    }

    @Override
    public void drive(Object attachment) {
        getState().frontendHandle(this, attachment);
    }


}
