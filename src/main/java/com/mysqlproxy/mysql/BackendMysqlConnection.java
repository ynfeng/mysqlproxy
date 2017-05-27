package com.mysqlproxy.mysql;

import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.InitialState;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by ynfeng on 2017/5/11.
 */
public final class BackendMysqlConnection extends MysqlConnection<MysqlPacket> {
    private String serverIp;
    private int serverPort;
    private FrontendMysqlConnection frontendMysqlConnection;

    public BackendMysqlConnection() {
        setState(InitialState.INSTANCE);
    }


    public void connect() throws IOException {
        getSocketChannel().connect(new InetSocketAddress(serverIp, serverPort));
    }

    public void close() {
        try {
            getSocketChannel().close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            myByteBuffAllocator.recyle(getReadBuffer());
            myByteBuffAllocator.recyle(getWriteBuffer());
            setReadBuff(null);
            setWriteBuff(null);
        }
    }

    public void drive(Object attachment) {
        getState().backendHandle(this, attachment);
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public FrontendMysqlConnection getFrontendMysqlConnection() {
        return frontendMysqlConnection;
    }

    public void setFrontendMysqlConnection(FrontendMysqlConnection frontendMysqlConnection) {
        this.frontendMysqlConnection = frontendMysqlConnection;
    }
}
