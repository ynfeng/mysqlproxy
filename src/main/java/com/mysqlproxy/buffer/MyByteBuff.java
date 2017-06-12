package com.mysqlproxy.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by ynfeng on 2017/5/15.
 * <p>
 * 此类非线程安全,当写时容量不足时会自动扩容，由于扩容时会分配ByteBuffer,而ByteBufferPool的分配操作必须
 * 由owner线程来执行，以减少同步消耗。所以此类有个使用限制：
 * 写操作尽量在owner线程中,如果不在owner线程中写，请确保有足够的剩余空间，可以通过ensureCapacity方法来确保
 * defaultSize的大小设置非常关键，大了造成内存的浪费，小了会频繁扩容
 * <p>
 * 此类提供Mysql协议基本数据类型的读取与转换
 * <p>
 * byteBufferArray          用于存放底层ByteBuffer
 * writeIndex               指出整个缓冲区的写位置
 * readIndex                指出整个缓冲区的读位置
 * curBufferArrayIndex      指出当前byteBufferArray的扩展位置
 * readBufferArrayIndex     指出byteBufferArray中读到哪个元索了
 * writeBufferArrayIndex    指出byteBufferArray中读写到哪个元索了
 * <p>
 * PS:扩容操作是个重量级操作，会涉及到数组拷贝，请尽量避免，如无法避免，请在owner线程中进行扩容
 */
public final class MyByteBuff {

    private final ByteBufferPool pool;
    private final MyByteBuffAllocator myByteBuffAllocator;
    private ByteBuffer[] byteBufferArray;
    private final int defaultSize;
    private int writeIndex;
    private int readIndex;
    private int capacity;
    private int freeBytes;
    private int curBufferArrayIndex;
    private int readBufferArrayIndex;
    private int writeBufferArrayIndex;
    private final long owner;


    public MyByteBuff(MyByteBuffAllocator myByteBuffAllocator, ByteBufferPool bufferPool, int defaultSize, long owner) {
        this.pool = bufferPool;
        byteBufferArray = new ByteBuffer[5];
        byteBufferArray[0] = pool.get(defaultSize);
        writeIndex = 0;
        readIndex = 0;
        curBufferArrayIndex = 0;
        capacity = defaultSize;
        freeBytes = defaultSize;
        this.defaultSize = defaultSize;
        this.readBufferArrayIndex = 0;
        this.writeBufferArrayIndex = 0;
        this.owner = owner;
        this.myByteBuffAllocator = myByteBuffAllocator;
    }

    private void ensureCapacity(int reqCapacity) {
        int needCapacity = reqCapacity - freeBytes;
        if (needCapacity > 0) {
            long currentThreadId = Thread.currentThread().getId();
            if (currentThreadId != owner) {
                throw new RuntimeException("can't allocate MyByteBuff in threadId " + currentThreadId);
            }
            int numOfByteBuff = needCapacity % defaultSize == 0 ? needCapacity / defaultSize : needCapacity / defaultSize + 1;
            int leftArrayNum = (byteBufferArray.length - curBufferArrayIndex) - 1;

            if (leftArrayNum < numOfByteBuff) {
                expandByteBufferArray(numOfByteBuff < 5 ? 5 : numOfByteBuff);
            }
            do {
                expandByteBuffer();
            } while ((--numOfByteBuff) > 0);
        }
    }

    private void expandByteBuffer() {
        byteBufferArray[++curBufferArrayIndex] = pool.get(defaultSize);
        this.capacity += defaultSize;
        this.freeBytes += defaultSize;
    }

    private void expandByteBufferArray(int num) {
        if (curBufferArrayIndex < byteBufferArray.length) {
            //扩展数组
            ByteBuffer[] newByteBufferArray = new ByteBuffer[byteBufferArray.length + num];
            System.arraycopy(byteBufferArray, 0, newByteBufferArray, 0, byteBufferArray.length);
            this.byteBufferArray = newByteBufferArray;
        }
    }

    public void clear() {
        //释放底层ByteBuffer,只保留一个
        for (int i = 1; i <= curBufferArrayIndex; i++) {
            pool.recyle(byteBufferArray[i]);
            byteBufferArray[i] = null;
        }
        byteBufferArray[0].clear();
        writeIndex = 0;
        readIndex = 0;
        curBufferArrayIndex = 0;
        readBufferArrayIndex = 0;
        writeBufferArrayIndex = 0;
        freeBytes = defaultSize;
        capacity = defaultSize;
    }

    public int transferToChannel(SocketChannel socketChannel) throws IOException {
        int totalWrite = 0;
        long writed;
        byteBufferArray[readBufferArrayIndex].position(readIndex % defaultSize);
        byteBufferArray[readBufferArrayIndex].limit(writeIndex / defaultSize > readBufferArrayIndex ? defaultSize : writeIndex - (defaultSize * readBufferArrayIndex));
        while (readIndex < writeIndex) {
            writed = socketChannel.write(byteBufferArray, readBufferArrayIndex, 1);
            if(writed == 0){
                break;
            }
            readIndex += writed;
            totalWrite += writed;
            if (byteBufferArray[readBufferArrayIndex].limit() == byteBufferArray[readBufferArrayIndex].position()
                    && readBufferArrayIndex < writeBufferArrayIndex) {
                this.readBufferArrayIndex = readBufferArrayIndex + 1;
                byteBufferArray[readBufferArrayIndex].position(readIndex % defaultSize);
                byteBufferArray[readBufferArrayIndex].limit(writeIndex / defaultSize > readBufferArrayIndex ? defaultSize : writeIndex - (defaultSize * readBufferArrayIndex));
            }
        }
        return totalWrite;
    }

    public int transferFromChannel(SocketChannel socketChannel) throws IOException {
        int totalRead = 0;
        byteBufferArray[writeBufferArrayIndex].position(writeIndex % defaultSize);
        byteBufferArray[writeBufferArrayIndex].limit(defaultSize);
        long readed = socketChannel.read(byteBufferArray, writeBufferArrayIndex, 1);
        for (; ; ) {
            this.freeBytes -= readed;
            if (freeBytes == 0) {
                ensureCapacity(defaultSize);
            }
            if (readed == -1) {
                throw new IOException("channel closed!");
            } else if (readed == 0) {
                break;
            }
            writeIndex += readed;
            totalRead += readed;
            if (byteBufferArray[writeBufferArrayIndex].capacity() == byteBufferArray[writeBufferArrayIndex].position()) {
                this.writeBufferArrayIndex = ++writeBufferArrayIndex;
                byteBufferArray[writeBufferArrayIndex].position(writeIndex % defaultSize);
                byteBufferArray[writeBufferArrayIndex].limit(defaultSize);
            }
            readed = socketChannel.read(byteBufferArray, writeBufferArrayIndex, 1);
        }
        return totalRead;
    }

    public int getReadableBytes() {
        return writeIndex - readIndex;
    }

    public long getFixLenthInteger(int startPos, int len) {
        return getInt(startPos, len);
    }

    public long readFixLengthInteger(int len) {
        long rv = getInt(readIndex, len);
        readIndex += len;
        return rv;
    }

    private int getByteBufferArrayIndex(int pos) {
        int index = pos / defaultSize;
        return index;
    }

    private int getByteBufferOffset(int pos) {
        return pos % defaultSize;
    }

    private long getInt(int startPos, int len) {
        checkBounds(len);
        long rv = 0;
        ByteBuffer[] byteBufferArray = this.byteBufferArray;
        int index = getByteBufferArrayIndex(startPos);
        int offset = getByteBufferOffset(startPos);
        ByteBuffer byteBuffer = byteBufferArray[index];
        for (int i = 0; i < len; i++) {
            byte b = byteBuffer.get(offset++);
            rv |= (((long) b) & 0xFF) << (i * 8);
            if (offset >= byteBuffer.position()) {
                byteBuffer = byteBufferArray[++index];
                offset = 0;
            }
        }
        return rv;
    }

    public void writeInt(long val, int len) {
        this.writeInt(val, writeIndex, len);
    }


    public void writeString(String val) {
        this.writeBytes(val.getBytes(), writeIndex);
        this.writeInt(0, 1);
    }

    public long readLenenc() {
        int lenenc = (int) this.getFixLenthInteger(readIndex, 1);
        if (lenenc < 0xFB) {
            return this.readFixLengthInteger(1);
        } else if (lenenc == 0xFC) {
            return this.readFixLengthInteger(2);
        } else if (lenenc == 0xFE) {
            return this.readFixLengthInteger(3);
        } else {
            return this.readFixLengthInteger(4);
        }
    }

    public long getLenenc(int startPos) {
        int lenenc = (int) this.getFixLenthInteger(startPos, 1);
        if (lenenc < 0xFB) {
            return this.getFixLenthInteger(startPos, 1);
        } else if (lenenc == 0xFC) {
            return this.getFixLenthInteger(startPos, 2);
        } else if (lenenc == 0xFE) {
            return this.getFixLenthInteger(startPos, 3);
        } else {
            return this.getFixLenthInteger(startPos, 4);
        }
    }

    public void writeLenecInt(long val, int len) {
        if (len < 0xFB) {
            this.writeInt(val, 1);
        } else if (len == 2) {
            this.writeInt(0xFC, 1);
            this.writeInt(val, 2);
        } else if (len == 3) {
            this.writeInt(0xFE, 1);
            this.writeInt(val, 3);
        } else {
            this.writeInt(0xFD, 1);
            this.writeInt(val, 8);
        }
    }

    public void writeLenencBytes(byte[] bytes) {
        int lenenc = bytes.length;
        if (lenenc < 0xFB) {
            this.writeInt(lenenc, 1);
        } else if (lenenc == 0xFC) {
            this.writeInt(lenenc, 2);
        } else if (lenenc == 0xFE) {
            this.writeInt(lenenc, 3);
        } else {
            this.writeInt(lenenc, 4);
        }
        this.writeBytes(bytes, writeIndex);
    }

    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, writeIndex);
    }

    private void writeBytes(byte[] bytes, int startPos) {
        ensureCapacity(bytes.length);
        int index = getByteBufferArrayIndex(startPos);
        int offset = getByteBufferOffset(startPos);
        ByteBuffer byteBuffer = byteBufferArray[index];
        for (int i = 0; i < bytes.length; i++) {
            byteBuffer.put(offset++, bytes[i]);
            byteBuffer.position(byteBuffer.position() + 1);
            freeBytes--;
            writeIndex++;
            this.writeBufferArrayIndex = index;
            if (offset >= byteBuffer.capacity() && index < curBufferArrayIndex) {
                byteBuffer = byteBufferArray[++index];
                offset = 0;
            }
        }
    }

    private void writeInt(long val, int startPos, int len) {
        ensureCapacity(len);
        int index = getByteBufferArrayIndex(startPos);
        int offset = getByteBufferOffset(startPos);
        ByteBuffer byteBuffer = byteBufferArray[index];
        for (int i = 0; i < len; i++) {
            byte b = (byte) ((val >> (i * 8)) & 0xFF);
            byteBuffer.put(offset++, b);
            byteBuffer.position(byteBuffer.position() + 1);
            freeBytes--;
            writeIndex++;
            this.writeBufferArrayIndex = index;
            if (offset >= byteBuffer.capacity() && index < curBufferArrayIndex) {
                byteBuffer = byteBufferArray[++index];
                offset = 0;
            }
        }
    }

    public String getFixString(int startPos, int len) {
        return getString(startPos, len);
    }

    public String readFixString(int len) {
        String str = getString(readIndex, len);
        readIndex += str.getBytes().length;
        readBufferArrayIndex = getByteBufferArrayIndex(readIndex);
        return str;
    }

    public String readNulTerminatedString() {
        String str = getNulTerminatedString(readIndex);
        readIndex += str.getBytes().length + 1;
        readBufferArrayIndex = getByteBufferArrayIndex(readIndex);
        return str;
    }

    public String getNulTerminatedString(int startPos) {
        ByteBuffer[] byteBufferArray = this.byteBufferArray;
        int index = getByteBufferArrayIndex(startPos);
        int offset = getByteBufferOffset(startPos);
        ByteBuffer byteBuffer = byteBufferArray[index];
        int nulIndex = 0;
        for (; ; ) {
            for (int i = offset; i < byteBuffer.position(); i++) {
                if (byteBuffer.get(i) == 0) {
                    nulIndex = index * defaultSize + i;
                    break;
                }
            }
            if (nulIndex == 0) {
                if (index + 1 > curBufferArrayIndex) {
                    return null;
                }
                //未找到结束标志，跳到一下个
                byteBuffer = byteBufferArray[++index];

                offset = 0;
            } else {
                break;
            }
        }
        int stringLen = nulIndex - startPos;
        return getString(startPos, stringLen);
    }

    public String readEOFString() {
        String str = getEOFString(readIndex);
        readIndex += str.getBytes().length;
        readBufferArrayIndex = getByteBufferArrayIndex(readIndex);
        return str;
    }

    public byte[] readBytes(int len) {
        byte[] bytes = getBytes(readIndex, len);
        readIndex += bytes.length;
        readBufferArrayIndex = getByteBufferArrayIndex(readIndex);
        return bytes;
    }

    public String getEOFString(int startPos) {
        int stringLen = writeIndex - startPos;
        return getString(startPos, stringLen);
    }

    private String getString(int startPos, int len) {
        return new String(getBytes(startPos, len));
    }

    private byte[] getBytes(int startPos, int len) {
        checkBounds(len);
        int index = getByteBufferArrayIndex(startPos);
        int offset = getByteBufferOffset(startPos);
        ByteBuffer byteBuffer = byteBufferArray[index];
        byte[] bytes = new byte[len];
        int bIndex = 0;
        for (int i = 0; i < len; i++) {
            bytes[bIndex++] = byteBuffer.get(offset++);
            if (offset >= byteBuffer.position()) {
                byteBuffer = byteBufferArray[++index];
                offset = 0;
            }
        }
        return bytes;
    }

    public void skip(int c) {
        checkBounds(c);
        readIndex += c;
        if (readIndex > ((readBufferArrayIndex + 1) * defaultSize)) {
            readBufferArrayIndex = getByteBufferArrayIndex(readIndex);
        }
    }

    private void checkBounds(int bytes) {
        if (readIndex + bytes > writeIndex) {
            throw new IndexOutOfBoundsException(String.format("can't read %d bytes from MyByteBuff", bytes));
        }
    }

    public boolean hasRemaining() {
        return readIndex < writeIndex;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWriteIndex() {
        return writeIndex;
    }

    public void setWriteIndex(int writeIndex) {
        this.writeIndex = writeIndex;
    }

    public int getReadIndex() {
        return readIndex;
    }

    public void setReadIndex(int readIndex) {
        this.readIndex = readIndex;
    }

    public int getFreeBytes() {
        return freeBytes;
    }

    public void setFreeBytes(int freeBytes) {
        this.freeBytes = freeBytes;
    }
}
