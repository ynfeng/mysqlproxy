package com.mysqlproxy.mysql;


import com.mysqlproxy.buffer.MyByteBuff;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by ynfeng on 2017/5/12.
 */
public interface NIOConnection {
    SocketChannel getSocketChannel();

    void setSocketChannel(SocketChannel socketChannel);

    SelectionKey getSelectionKey();

    void setSelectionKey(SelectionKey selectionKey);

    Selector getSelector();

    void setSelector(Selector selector);

    MyByteBuff getReadBuffer();

    MyByteBuff getWriteBuffer();
}
