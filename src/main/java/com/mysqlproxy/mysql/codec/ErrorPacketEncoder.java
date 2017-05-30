package com.mysqlproxy.mysql.codec;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.ErrorPacket;

import java.io.IOException;

public class ErrorPacketEncoder implements Encoder<ErrorPacket> {
    public static final ErrorPacketEncoder INSTANCE = new ErrorPacketEncoder();

    private ErrorPacketEncoder() {
    }

    @Override
    public void encode(ErrorPacket errorPacket, MyByteBuff out) throws IOException {
        out.writeInt(errorPacket.payloadLength,3);
        out.writeInt(errorPacket.sequenceId,1);
        out.writeInt(errorPacket.header,1);
        out.writeInt(errorPacket.errCode,2);
        out.writeBytes(errorPacket.sqlStateMarker.getBytes());
        out.writeBytes(errorPacket.sqlState.getBytes());
        out.writeBytes(errorPacket.errMsg.getBytes());
    }

}
