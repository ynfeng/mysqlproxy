package com.mysqlproxy.mysql.codec;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.OKPacket;

import java.io.IOException;

public class OKPacketEncoder implements Encoder<OKPacket> {
    public static final OKPacketEncoder INSTANCE = new OKPacketEncoder();

    private OKPacketEncoder() {
    }

    @Override
    public void encode(OKPacket okPacket, MyByteBuff out) throws IOException {
            out.writeInt(okPacket.payloadLength,3);
            out.writeInt(okPacket.sequenceId,1);

            out.writeInt(okPacket.header,1);
            out.writeLenecInt(okPacket.affectedRows,1);
            out.writeLenecInt(okPacket.lastInsertId,1);
            out.writeInt(okPacket.statusFlags,2);
            out.writeInt(okPacket.warnings,2);
    }
}
