package com.mysqlproxy.mysql.codec;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.HandshakeResponse41Packet;
import com.mysqlproxy.mysql.protocol.MysqlPacket;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/20.
 * <p>
 * 握手响应包
 */
public class ResponseHandshakeCodec implements Decoder<MysqlPacket>, Encoder<MysqlPacket> {
    public static final ResponseHandshakeCodec INSTANCE = new ResponseHandshakeCodec();

    private ResponseHandshakeCodec() {

    }

    @Override
    public MysqlPacket decode(MyByteBuff buff) throws IOException {
        int packageLenth = (int) buff.getFixLenthInteger(0, 3);
        if (packageLenth >= buff.getReadableBytes() - 4) {
            buff.skip(3);
            byte sequenceId = (byte) buff.readFixLengthInteger(1);
            HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet(packageLenth, sequenceId);
            handshakeResponse41Packet.capability = (int) buff.readFixLengthInteger(4);
            handshakeResponse41Packet.maxPacketSize = (int) buff.readFixLengthInteger(4);
            handshakeResponse41Packet.characterSet = (byte) buff.readFixLengthInteger(1);
            buff.skip(23);
            handshakeResponse41Packet.username = buff.readNulTerminatedString();
            int lengthOfAuth = (int) buff.readLenenc();
            handshakeResponse41Packet.authData = buff.readBytes(lengthOfAuth);
            handshakeResponse41Packet.authPluginName = buff.readNulTerminatedString();
            return handshakeResponse41Packet;
        }
        return null;
    }

    @Override
    public void encode(MysqlPacket packet, MyByteBuff out) throws IOException {
        HandshakeResponse41Packet handshakeResponse41Packet = (HandshakeResponse41Packet) packet;
        out.writeInt(handshakeResponse41Packet.payloadLength, 3);
        out.writeInt(handshakeResponse41Packet.sequenceId, 1);
        out.writeInt(handshakeResponse41Packet.capability, 4);
        out.writeInt(handshakeResponse41Packet.maxPacketSize, 4);
        out.writeInt(handshakeResponse41Packet.characterSet, 1);
        out.writeInt(0, 8);
        out.writeInt(0, 8);
        out.writeInt(0, 7);
        out.writeString(handshakeResponse41Packet.username);
        out.writeLenencBytes(handshakeResponse41Packet.authData);
        out.writeString(handshakeResponse41Packet.authPluginName);
    }
}
