package com.mysqlproxy.mysql;

import com.mysqlproxy.buffer.MyByteBuff;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/11.
 */
public interface Connection {

    void close();

    MyByteBuff read() throws IOException;

    int write(MyByteBuff buff) throws IOException;

}
