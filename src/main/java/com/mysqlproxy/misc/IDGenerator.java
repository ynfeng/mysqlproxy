package com.mysqlproxy.misc;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private AtomicInteger id = new AtomicInteger();

    public IDGenerator() {
    }

    public int get() {
        return id.incrementAndGet();
    }
}
