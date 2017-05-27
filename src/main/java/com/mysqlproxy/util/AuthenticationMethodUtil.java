package com.mysqlproxy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by ynfeng on 2017/4/9.
 */
public class AuthenticationMethodUtil {

    public static byte[] generateMysqlNativePassword(String password, String authPluginData) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest
                    .getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.update(password.getBytes());
        byte[] sha1PassWordBytes = digest.digest();
        digest.reset();

        digest.update(sha1PassWordBytes);
        byte[] part2Bytes = digest.digest();
        digest.reset();

        digest.update(authPluginData.getBytes());
        digest.update(part2Bytes);

        part2Bytes = digest.digest();
        for (int i = 0; i < sha1PassWordBytes.length; i++) {
            sha1PassWordBytes[i] = (byte) (sha1PassWordBytes[i] ^ part2Bytes[i]);
        }
        return sha1PassWordBytes;
    }

    public static byte[] randomString(int len) {
        byte[] bytes = new byte[len];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ThreadLocalRandom.current().nextInt(33, 128);
        }
        return bytes;
    }


    private static String toStringHex(byte[] data) {
        StringBuffer hexString = new StringBuffer();
        // 字节数组转换为 十六进制 数
        for (int i = 0; i < data.length; i++) {
            String shaHex = Integer.toHexString(data[i] & 0xFF);
            if (shaHex.length() < 2) {
                hexString.append(0);
            }
            hexString.append(shaHex);
        }
        return hexString.toString();
    }
}
