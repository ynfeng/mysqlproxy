package com.mysqlproxy.mysql;


import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class MysqlBackendConnectionPool {
    private ConcurrentLinkedQueue<BackendMysqlConnection> queue = new ConcurrentLinkedQueue();

    public void add(BackendMysqlConnection mysqlBackendMysqlConnection) {
        queue.offer(mysqlBackendMysqlConnection);
    }

    public BackendMysqlConnection get() {
        return queue.poll();
    }

}
