package com.mysqlproxy.mysql.protocol;


/**
 * Created by ynfeng on 2017/5/16.
 */
public class InitialHandshakeV10Packet extends MysqlPacket {
    public byte protocolVersion;
    public String serverVersion;
    public int connectionId;
    public String authPluginDataPart;
    public byte[] authPluginDataPart1;
    public byte[] authPluginDataPart2;
    public int capabilityFlags;
    public byte characterSet;
    public short statusFlag;
    public byte lengthOfAuthPluginData;
    public String authPluginName;

    public InitialHandshakeV10Packet(int payloadLength, byte sequenceId) {
        super(payloadLength, sequenceId);
    }
}
