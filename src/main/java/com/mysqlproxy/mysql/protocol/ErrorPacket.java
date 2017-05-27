package com.mysqlproxy.mysql.protocol;


/**
 * Created by ynfeng on 2017/5/20.
 *
 * mysqlError包
 */
public class ErrorPacket extends MysqlPacket{
    public byte header;
    public short errCode;
    public String errMsg;
    public String sqlStateMarker;
    public String sqlState;

    public ErrorPacket(int payloadLength, byte sequenceId) {
        super(payloadLength, sequenceId);
    }
}
