package com.mysqlproxy.buffer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ynfeng on 2017/5/17.
 * <p>
 * 有关内存分配所有的操作的类都与一个线程绑定，如果不在绑定线程上分配分有异常抛出
 */
public final class MyByteBuffAllocator {
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
        MyByteBuff myByteBuff = freeBuffers.poll();
        if (myByteBuff == null) {
            myByteBuff = new MyByteBuff(this, byteBufferPool, size, owner);
        }
        return myByteBuff;
    }

    public boolean recyle(MyByteBuff myByteBuff) {
        long currentThreadId = Thread.currentThread().getId();
        if (currentThreadId != owner) {
            throw new RuntimeException("can't recyle MyByteBuff in threadId " + currentThreadId);
        }
        myByteBuff.clear();
        freeBuffers.offer(myByteBuff);
        return true;
    }
}
