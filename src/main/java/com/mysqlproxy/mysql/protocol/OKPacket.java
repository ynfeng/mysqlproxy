package com.mysqlproxy.mysql.protocol;

/**
 * Created by ynfeng on 2017/5/25.
 */
public class OKPacket extends MysqlPacket {
    public byte header;
    public long affectedRows;
    public long lastInsertId;
    public short statusFlags;
    public short warnings;
    public String info;


    public OKPacket(int payloadLength, byte sequenceId) {
        super(payloadLength, sequenceId);
    }
}
