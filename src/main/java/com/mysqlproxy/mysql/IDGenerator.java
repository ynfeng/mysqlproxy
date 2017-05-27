package com.mysqlproxy.mysql;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private AtomicInteger id = new AtomicInteger();

    public IDGenerator() {
    }

    public int get() {
        return id.incrementAndGet();
    }
}
