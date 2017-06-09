package com.mysqlproxy.net;

import com.mysqlproxy.buffer.ByteBufferPool;
import com.mysqlproxy.buffer.MyByteBuffAllocator;
import com.mysqlproxy.mysql.FrontendMysqlConnection;
import com.mysqlproxy.mysql.MysqlConnection;
import com.mysqlproxy.mysql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ynfeng on 2017/5/12.
 * <p>
 * 每个线程都有一个ByteBufferPool,MyByteBuffAllocator
 * 分配和回收动作必须在Reactor线程中执行，以减少同步消耗
 */
public class Reactor {
    private Logger logger = LoggerFactory.getLogger(Reactor.class);
    private final Selector selector;
    private final ConcurrentLinkedQueue<Connection> registerQueue;
    private final ByteBufferPool byteBufferPool;
    private final MyByteBuffAllocator myByteBuffAllocator;
    private final Thread reactorThread;

    public Reactor() throws IOException {
        this.selector = Selector.open();
        reactorThread = new ReactorThread();
        registerQueue = new ConcurrentLinkedQueue();
        byteBufferPool = new ByteBufferPool(reactorThread.getId());
        myByteBuffAllocator = new MyByteBuffAllocator(byteBufferPool, reactorThread.getId());

    }

    public void register(MysqlConnection mysqlConnection) {
        mysqlConnection.setReactor(this);
        registerQueue.offer(mysqlConnection);
        selector.wakeup();
    }

    public void startup() {
        reactorThread.start();
    }

    class ReactorThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int c = selector.select();
                    processRegister();
                    if (c > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        for (SelectionKey key : keys) {
                            if (key.isValid()) {
                                MysqlConnection connection = (MysqlConnection) key.attachment();
                                logger.debug(String.format(
                                        "Reactor线程循环，有%d个事件。%s isWriteMode = %b,isReadMode = %b",
                                        keys.size(),
                                        connection.toString(),
                                        connection.isWriteMode(),
                                        connection.isReadMode()
                                ));
                                connection.drive(null);
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

    private void processRegister() {
        Connection connection;
        while ((connection = registerQueue.poll()) != null) {
            try {
                MysqlConnection mysqlConnection = ((MysqlConnection) connection);
                SelectionKey key = mysqlConnection.getSocketChannel()
                        .register(selector, SelectionKey.OP_READ);
                mysqlConnection.setSelectionKey(key);
                mysqlConnection.setMyByteBuffAllocator(myByteBuffAllocator);
                key.attach(connection);
                if (mysqlConnection instanceof FrontendMysqlConnection) {
                    mysqlConnection.drive(null);
                }
            } catch (ClosedChannelException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
