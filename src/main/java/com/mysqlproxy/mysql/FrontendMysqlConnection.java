package com.mysqlproxy.mysql;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.InitialState;

import java.io.IOException;


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

    public int writeInDirectTransferMode(MyByteBuff myByteBuff) throws IOException {
        int writed = myByteBuff.transferToChannel(getSocketChannel());
        setDirectTransferPacketWriteLen(getDirectTransferPacketWriteLen() + writed);
        getBackendMysqlConnection().setDirectTransferPacketWriteLen(getBackendMysqlConnection().getDirectTransferPacketWriteLen() + writed);
        return writed;
    }
}
