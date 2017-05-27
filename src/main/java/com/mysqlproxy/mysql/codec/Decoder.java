package com.mysqlproxy.mysql.codec;

import com.mysqlproxy.buffer.MyByteBuff;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/16.
 */
public interface Decoder<T> {
    T decode(MyByteBuff buff) throws IOException;
}
