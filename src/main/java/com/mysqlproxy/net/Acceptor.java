package com.mysqlproxy.net;

import com.mysqlproxy.ServerContext;
import com.mysqlproxy.mysql.BackendMysqlConnection;
import com.mysqlproxy.mysql.FrontendMysqlConnection;

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
                                //TODO 还未实现后端连接池,连接池实现后尝试从连接池中取后端连接
                                BackendMysqlConnection mysqlBackendMysqlConnection = ServerContext.getInstance().getBackendMysqlConnectionFactory().create("10.211.55.5", 3306);
                                FrontendMysqlConnection frontendMysqlConnection = ServerContext.getInstance().getFrontendMysqlConnectionFactory().create(socketChannel, mysqlBackendMysqlConnection);
                                mysqlBackendMysqlConnection.setFrontendMysqlConnection(frontendMysqlConnection);
                                /**
                                 * 后端连接池中的连接必定属于一个Reactor,所以前端连接使用和后端连接的Reactor
                                 * 新建的后端连接没有Reactor,所以前端连接随机注册到一个Reactor,后面后端连接会注册到和前端连接相同的Reactor
                                 * 保证前后端连接与同一个Reactor绑定
                                 */
                                if (mysqlBackendMysqlConnection.getReactor() == null) {
                                    ServerContext.getInstance().getMultiReactor().postRegister(frontendMysqlConnection);
                                } else {
                                    mysqlBackendMysqlConnection.getReactor().register(frontendMysqlConnection);
                                }
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
