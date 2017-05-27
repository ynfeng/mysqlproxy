package com.mysqlproxy.mysql.protocol;


/**
 * Created by ynfeng on 2017/5/16.
 * <p>
 * Mysql包，考虑将此类的子类放入对象池中中来减少GC?
 */
public abstract class MysqlPacket {
    public int payloadLength;
    public byte sequenceId;


    public MysqlPacket(int payloadLength, byte sequenceId) {
        this.payloadLength = payloadLength;
        this.sequenceId = sequenceId;
    }

}
