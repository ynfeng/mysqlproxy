package com.mysqlproxy.mysql.codec;


import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.protocol.InitialHandshakeV10Packet;
import com.mysqlproxy.mysql.protocol.MysqlPacket;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/18.
 * <p>
 * 初始握手包编解码器
 */
public class InitalHandshakeCodec implements Decoder<MysqlPacket>, Encoder<InitialHandshakeV10Packet> {
    public final static InitalHandshakeCodec INSTANCE = new InitalHandshakeCodec();

    private InitalHandshakeCodec() {

    }

    @Override
    public MysqlPacket decode(MyByteBuff buff) throws IOException {
        int packetLen = (int) buff.getFixLenthInteger(0, 3);
        byte sequenceId = (byte) buff.getFixLenthInteger(3, 1);
        if (buff.getReadableBytes() - 4 >= packetLen) {
            buff.skip(4);
            long header = buff.readFixLengthInteger(1);
            if (header == 0xff) {
                ErrorPacket errorPacket = new ErrorPacket(packetLen, sequenceId);
                errorPacket.errCode = (short) buff.readFixLengthInteger(2);
                errorPacket.errMsg = buff.readEOFString();
                return errorPacket;
            } else if (header == 0x0a) {
                //握手包解析
                InitialHandshakeV10Packet initialHandshakeV10Packet = new InitialHandshakeV10Packet(packetLen, sequenceId);
                initialHandshakeV10Packet.protocolVersion = (byte) header;
                initialHandshakeV10Packet.serverVersion = buff.readNulTerminatedString();
                initialHandshakeV10Packet.connectionId = (int) buff.readFixLengthInteger(4);
                String authPluginDataPart1 = buff.readNulTerminatedString();
                short capabilityLower = (short) buff.readFixLengthInteger(2);
                initialHandshakeV10Packet.characterSet = (byte) buff.readFixLengthInteger(1);
                initialHandshakeV10Packet.statusFlag = (short) buff.readFixLengthInteger(2);
                short capabilityUpper = (short) buff.readFixLengthInteger(2);
                initialHandshakeV10Packet.capabilityFlags = (capabilityUpper << 16) | (capabilityLower & 0xFFFF);
                initialHandshakeV10Packet.lengthOfAuthPluginData = (byte) buff.readFixLengthInteger(1);
                buff.skip(10);
                String authPluginDataPart2 = buff.readNulTerminatedString();
                initialHandshakeV10Packet.authPluginDataPart = authPluginDataPart1 + authPluginDataPart2;
                String authPluginName = buff.readNulTerminatedString();
                initialHandshakeV10Packet.authPluginName = authPluginName;
                return initialHandshakeV10Packet;
            } else {
                //错误的包
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void encode(InitialHandshakeV10Packet packet, MyByteBuff out) throws IOException {
        out.writeInt(packet.payloadLength,3);
        out.writeInt(packet.sequenceId,1);
        out.writeInt(packet.protocolVersion,1);
        out.writeString(packet.serverVersion);
        out.writeInt(packet.connectionId,4);
        out.writeBytes(packet.authPluginDataPart1);
        out.writeInt(0,1);
        out.writeInt(packet.capabilityFlags & 0xFFFF,2);
        out.writeInt(packet.characterSet,1);
        out.writeInt(packet.statusFlag,2);
        out.writeInt((packet.capabilityFlags >> 16) & 0xFFFF,2);
        out.writeInt(packet.lengthOfAuthPluginData,1);
        out.writeInt(0,5);
        out.writeInt(0,5);
        out.writeBytes(packet.authPluginDataPart2);
        out.writeInt(0,1);
        out.writeString(packet.authPluginName);
    }

}
