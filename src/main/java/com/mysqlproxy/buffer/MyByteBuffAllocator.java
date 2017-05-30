package com.mysqlproxy.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ynfeng on 2017/5/17.
 * <p>
 * 有关内存分配所有的操作的类都与一个线程绑定，如果不在绑定线程上分配分有异常抛出
 */
public final class MyByteBuffAllocator {
    private Logger logger = LoggerFactory.getLogger(MyByteBuffAllocator.class);
    private final ByteBufferPool byteBufferPool;
    private final Queue<MyByteBuff> freeBuffers;
    private final long owner;

    public MyByteBuffAllocator(ByteBufferPool byteBufferPool, long owner) {
        this.byteBufferPool = byteBufferPool;
        freeBuffers = new LinkedList<>();
        this.owner = owner;
    }

    public MyByteBuff allocate(int size) {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != owner) {
            throw new RuntimeException("can't allocate MyByteBuff in threadId " + currentThreadId);
        }
        logger.debug("分配MyByteBuffer,大小:{}，绑定线程:{}", size, owner);
        MyByteBuff myByteBuff = freeBuffers.poll();
        if (myByteBuff == null) {
            logger.debug("没有找到空闲的MyByteBuffer，绑定线程:{}", owner);
            myByteBuff = new MyByteBuff(this, byteBufferPool, size, owner);
        }
        logger.debug("分配MyByteBuffer,大小:{},绑定线程:{}", size, owner);
        return myByteBuff;
    }

    public boolean recyle(MyByteBuff myByteBuff) {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != owner) {
            throw new RuntimeException("can't recyle MyByteBuff in threadId " + currentThreadId);
        }
        logger.debug("回收MyByteBuff,大小:{},绑定线程:{}", myByteBuff.getCapacity(),owner);
        myByteBuff.clear();
        freeBuffers.offer(myByteBuff);
        return true;
    }
}
