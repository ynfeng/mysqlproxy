package com.mysqlproxy.net;

import com.mysqlproxy.ServerContext;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class Acceptor {
    private Logger logger = LoggerFactory.getLogger(Acceptor.class);
    private Selector selector;
    private SelectionKey selectionKey;
    private ServerSocketChannel serverSocketChannel;

    public Acceptor() {
        try {
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(3306));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            selectionKey = serverSocketChannel.register(selector, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startup() {
        logger.info("服务器启动，监听端口3306");
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        new AcceptorThread().start();
    }

    class AcceptorThread extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int c = 0;
                try {
                    c = selector.select();
                    if (c > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> it = keys.iterator();
                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            if (key.isValid() && key.isAcceptable()) {
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                socketChannel.configureBlocking(false);
                                FrontendMysqlConnection frontendMysqlConnection = ServerContext.getInstance().getFrontendMysqlConnectionFactory().create(socketChannel);
                                ServerContext.getInstance().getMultiReactor().postRegister(frontendMysqlConnection);
                            }
                        }
                        keys.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
}
