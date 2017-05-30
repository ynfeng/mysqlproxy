package com.mysqlproxy.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

/**
 * Created by ynfeng on 2017/5/15.
 * <p>
 * 非线程安全的类,用于分配底层的ByteBuffer
 */
public final class ByteBufferPool {
    private Logger logger = LoggerFactory.getLogger(ByteBufferPool.class);
    private TreeSet<Chunk> index = new TreeSet();
    private final long owner;

    public ByteBufferPool(long owner) {
        this.owner = owner;
    }

    public ByteBuffer get(int reqCapacity) {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != owner) {
            throw new RuntimeException("can't allocate ByteBuffer in threadId " + currentThreadId);
        }
        logger.debug("分配ByteBuffer，大小:{},绑定线程{}",reqCapacity,owner);
        Chunk newChunk = new Chunk((int) (reqCapacity));
        Chunk find = index.ceiling(newChunk);
        if (find == null) {
            logger.debug("没有空闲的ByteBuffer,绑定线程{}",owner);
            index.add(newChunk);
            find = newChunk;
        }
        return find.allocate();
    }

    public boolean recyle(ByteBuffer byteBuffer) {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != owner) {
            throw new RuntimeException("can't recyle ByteBuffer in threadId " + currentThreadId);
        }
        boolean success = false;
        for (Chunk chunk : index) {
            if (chunk.getChunkSize() == byteBuffer.capacity()) {
                byteBuffer.clear();
                success = chunk.recyle(byteBuffer);
                break;
            }
        }
        return success;
    }

    private class Chunk implements Comparable<Chunk> {
        private int chunkSize;
        private Queue<ByteBuffer> freeBuffers = new LinkedList<>();

        public Chunk(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public boolean recyle(ByteBuffer byteBuffer) {
            logger.debug("回收ByteBuffer,大小{},绑定线程{}",byteBuffer.capacity(),owner);
            if (byteBuffer.capacity() == chunkSize) {
                freeBuffers.offer(byteBuffer);
                return true;
            }
            return false;
        }

        public ByteBuffer allocate() {
            ByteBuffer byteBuffer = freeBuffers.poll();
            if (byteBuffer == null) {
                byteBuffer = ByteBuffer.allocateDirect(chunkSize);
            }
            byteBuffer.clear();
            return byteBuffer;
        }

        @Override
        public int compareTo(Chunk chunk) {
            return chunkSize - chunk.getChunkSize();
        }

        public int getChunkSize() {
            return chunkSize;
        }
    }
}
