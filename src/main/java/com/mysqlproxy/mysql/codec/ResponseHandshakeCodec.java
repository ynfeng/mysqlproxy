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
        return null;
    }

    @Override
    public void encode(MysqlPacket packet, MyByteBuff out) throws IOException {
        HandshakeResponse41Packet handshakeResponse41Packet = (HandshakeResponse41Packet)packet;
        out.writeInt(handshakeResponse41Packet.payloadLength,3);
        out.writeInt(handshakeResponse41Packet.sequenceId,1);
        out.writeInt(handshakeResponse41Packet.capability,4);
        out.writeInt(handshakeResponse41Packet.maxPacketSize,4);
        out.writeInt(handshakeResponse41Packet.characterSet,1);
        out.writeInt(0,8);
        out.writeInt(0,8);
        out.writeInt(0,7);
        out.writeString(handshakeResponse41Packet.username);
        out.writeLenenc(handshakeResponse41Packet.authData);
        out.writeString(handshakeResponse41Packet.authPluginName);
    }
}
