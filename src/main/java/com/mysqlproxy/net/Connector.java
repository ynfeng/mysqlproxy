package com.mysqlproxy.net;

import com.mysqlproxy.ServerContext;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class Connector {
    private Logger logger = LoggerFactory.getLogger(Connector.class);
    private Selector selector;
    private ConcurrentLinkedQueue<BackendMysqlConnection> connectQueue = new ConcurrentLinkedQueue();

    public Connector() throws IOException {
        selector = Selector.open();
    }

    public void startup() {
        new ConnectorThread().start();
    }

    public void connect(BackendMysqlConnection backendMysqlConnection) {
        logger.debug("新建后端连接");
        connectQueue.offer(backendMysqlConnection);
        selector.wakeup();
    }

    public void processConnect() {
        BackendMysqlConnection connection;
        while ((connection = connectQueue.poll()) != null) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                connection.setSocketChannel(socketChannel);
                connection.setSelector(selector);
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
                connection.setSelectionKey(key);
                key.attach(connection);
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    class ConnectorThread extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int c = selector.select();
                    processConnect();
                    if (c > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        for (SelectionKey key : keys) {
                            ((SocketChannel) key.channel()).finishConnect();
                            key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
                            BackendMysqlConnection connection = (BackendMysqlConnection) key.attachment();
                            if (connection.getFrontendMysqlConnection() != null &&
                                    connection.getFrontendMysqlConnection().getReactor() == null) {
                                connection.getFrontendMysqlConnection().getReactor().register(connection);
                            } else {
                                ServerContext.getInstance().getMultiReactor().postRegister(connection);
                            }
                        }
                        keys.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
