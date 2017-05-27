package com.mysqlproxy.mysql.protocol;

/**
 * Created by ynfeng on 2017/5/20.
 */
public class CapabilityFlags {
    public final static int CLIENT_LONG_PASSWORD = 0x00000001;
    public final static int CLIENT_FOUND_ROWS = 0x00000002;
    public final static int CLIENT_LONG_FLAG = 0x00000004;
    public final static int CLIENT_CONNECT_WITH_DB = 0x00000008;
    public final static int CLIENT_NO_SCHEMA = 0x00000010;
    public final static int CLIENT_COMPRESS = 0x00000020;
    public final static int CLIENT_ODBC = 0x00000040;
    public final static int CLIENT_LOCAL_FILES = 0x00000080;
    public final static int CLIENT_IGNORE_SPACE = 0x00000100;
    public final static int CLIENT_PROTOCOL_41 = 0x00000200;
    public final static int CLIENT_INTERACTIVE = 0x00000400;
    public final static int CLIENT_SSL = 0x00000800;
    public final static int CLIENT_IGNORE_SIGPIPE = 0x00001000;
    public final static int CLIENT_TRANSACTIONS = 0x00002000;
    public final static int CLIENT_RESERVED = 0x00004000;
    public final static int CLIENT_SECURE_CONNECTION = 0x00008000;
    public final static int CLIENT_MULTI_STATEMENTS = 0x00010000;
    public final static int CLIENT_MULTI_RESULTS = 0x00020000;
    public final static int CLIENT_PS_MULTI_RESULTS = 0x00040000;
    public final static int CLIENT_PLUGIN_AUTH = 0x00080000;
    public final static int CLIENT_CONNECT_ATTRS = 0x00100000;
    public final static int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
    public final static int CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = 0x00400000;
    public final static int CLIENT_SESSION_TRACK = 0x00800000;
    public final static int CLIENT_DEPRECATE_EOF = 0x01000000;
    public final static int UNUSED = 0x80000000;
}
