package com.mysqlproxy;

import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.BackendMysqlConnectionFactory;
import com.mysqlproxy.net.Acceptor;
import com.mysqlproxy.net.Connector;
import com.mysqlproxy.net.MultiReactor;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class ServerBootstrap {
    private void preInit() throws IOException {
        ServerContext serverContext = ServerContext.getInstance();

        Connector connector = new Connector();
        serverContext.setConnector(connector);

        MultiReactor multiReactor = new MultiReactor();
        serverContext.setMultiReactor(multiReactor);
    }

    private void postInit() {
//        BackendMysqlConnection conn = BackendMysqlConnectionFactory.INSTANCE.create("10.211.55.5", 3306);
//        ServerContext.getInstance().getConnector().connect(conn);
    }


    public void startup() throws IOException {
        preInit();
        ServerContext.getInstance().getConnector().startup();
        ServerContext.getInstance().getMultiReactor().startup();
        new Acceptor().startup();
        postInit();

    }

    public static void main(String args[]) {

        try {
            new ServerBootstrap().startup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
