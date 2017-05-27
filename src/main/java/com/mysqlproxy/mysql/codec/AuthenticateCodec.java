package com.mysqlproxy.mysql.codec;

import com.mysqlproxy.buffer.MyByteBuff;
import com.mysqlproxy.mysql.protocol.ErrorPacket;
import com.mysqlproxy.mysql.protocol.MysqlPacket;
import com.mysqlproxy.mysql.protocol.OKPacket;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/24.
 */
public class AuthenticateCodec implements Decoder<MysqlPacket>, Encoder<MysqlPacket> {
    public static final AuthenticateCodec INSTANCE = new AuthenticateCodec();

    private AuthenticateCodec() {

    }

    @Override
    public MysqlPacket decode(MyByteBuff buff) throws IOException {
        int packetLen = (int) buff.getFixLenthInteger(0, 3);
        byte sequenceId = (byte) buff.getFixLenthInteger(3, 1);
        if (buff.getReadableBytes() - 4 >= packetLen) {
            buff.skip(4);
            long header = buff.readFixLengthInteger(1);
            if (header == 0) {
                //OK包
                OKPacket okPacket = new OKPacket(packetLen,sequenceId);
                okPacket.affectedRows = buff.readFixLengthInteger(1);
                okPacket.lastInsertId = buff.readFixLengthInteger(1);
                okPacket.statusFlags = (short) buff.readFixLengthInteger(2);
                okPacket.warnings = (short) buff.readFixLengthInteger(2);
                return okPacket;
            } else if (header == 0xff) {
                //ERR包
                ErrorPacket errorPacket = new ErrorPacket(packetLen, sequenceId);
                errorPacket.header = (byte) header;
                errorPacket.errCode = (short) buff.readFixLengthInteger(2);
                errorPacket.sqlStateMarker = buff.readFixString(1);
                errorPacket.sqlState = buff.readFixString(5);
                errorPacket.errMsg = buff.readEOFString();
                return errorPacket;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void encode(MysqlPacket packet, MyByteBuff out) throws IOException {

    }
}
