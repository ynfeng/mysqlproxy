package com.mysqlproxy.mysql;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.buffer.MyByteBuffAllocator;
import com.mysqlproxy.mysql.codec.Decoder;
import com.mysqlproxy.mysql.codec.Encoder;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.state.AuthenticatingState;
import com.mysqlproxy.mysql.state.CloseState;
import com.mysqlproxy.mysql.state.MysqlConnectionState;
import com.mysqlproxy.net.Reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by ynfeng on 2017/5/11.
 */
public abstract class MysqlConnection<T> implements Connection, StatefulConnection, NIOConnection {

    private MysqlConnectionState state;

    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private Selector selector;

    private MyByteBuff readBuff;
    private MyByteBuff writeBuff;
    private Reactor reactor;
    protected MyByteBuffAllocator myByteBuffAllocator;


    public void setState(MysqlConnectionState state) {
        this.state = state;
    }

    @Override
    public MysqlConnectionState getState() {
        return state;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    @Override
    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public MyByteBuff read() throws IOException {
        MyByteBuff readBuffer = getReadBuffer();
        if (readBuffer == null) {
            readBuffer = myByteBuffAllocator.allocate(1024 * 1024);
            setReadBuff(readBuffer);
        }
        readBuffer.transferFromChannel(getSocketChannel());
        return readBuffer;
    }

    public boolean flushWriteBuffer() throws IOException {
        MyByteBuff writeBuff = getWriteBuffer();
        write(writeBuff);
        if (!writeBuff.hasRemaining()) {
            return true;
        }
        return false;
    }

    public int write(MyByteBuff myByteBuff) throws IOException {
        return myByteBuff.transferToChannel(getSocketChannel());
    }

    public void disableRead() {
        getSelectionKey().interestOps(getSelectionKey().interestOps() & ~SelectionKey.OP_READ);
    }

    public void disableWriteAndEnableRead() {
        getSelectionKey().interestOps((getSelectionKey().interestOps() & ~SelectionKey.OP_WRITE) | SelectionKey.OP_READ);
    }

    public void enableWrite() {
        getSelectionKey().interestOps(getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
    }

    public MysqlPacket readPacket(Decoder<MysqlPacket> decoder) throws IOException {
        MyByteBuff buff = read();
        return decoder.decode(buff);
    }

    public void writePacket(MysqlPacket packet, Encoder<MysqlPacket> encoder) throws IOException {
        MyByteBuff buff = getWriteBuffer();
        if (buff == null) {
            buff = myByteBuffAllocator.allocate(1024 * 1024);
        }
        encoder.encode(packet, buff);
        setWriteBuff(buff);
        enableWrite();
    }

    @Override
    public MyByteBuff getReadBuffer() {
        return readBuff;
    }

    @Override
    public MyByteBuff getWriteBuffer() {
        return writeBuff;
    }

    public void setReadBuff(MyByteBuff readBuff) {
        this.readBuff = readBuff;
    }

    public void setWriteBuff(MyByteBuff writeBuff) {
        this.writeBuff = writeBuff;
    }

    public void setMyByteBuffAllocator(MyByteBuffAllocator myByteBuffAllocator) {
        this.myByteBuffAllocator = myByteBuffAllocator;
    }

    public Reactor getReactor() {
        return reactor;
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
    }
}
