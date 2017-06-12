package com.mysqlproxy.mysql.protocol;

/**
 * Created by mac on 2017/5/20.
 */
public class HandshakeResponse41Packet extends MysqlPacket {
    public int capability;
    public int maxPacketSize;
    public byte characterSet;
    public String username;
    public byte[] authData;
    public String authPluginName;
    public String schema;



    public HandshakeResponse41Packet(int payloadLength, byte sequenceId) {
        super(payloadLength, sequenceId);
    }


}
