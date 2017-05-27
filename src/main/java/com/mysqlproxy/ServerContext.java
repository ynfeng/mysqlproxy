package com.mysqlproxy;

import com.mysqlproxy.mysql.BackendMysqlConnectionFactory;
import com.mysqlproxy.mysql.FrontendMysqlConnectionFactory;
import com.mysqlproxy.mysql.IDGenerator;
import com.mysqlproxy.mysql.MysqlBackendConnectionPool;
import com.mysqlproxy.net.Connector;
import com.mysqlproxy.net.MultiReactor;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class ServerContext {
    private static final ServerContext INSTANCE = new ServerContext();
    private final MysqlBackendConnectionPool mysqlBackendConnectionPool = new MysqlBackendConnectionPool();
    private final FrontendMysqlConnectionFactory frontendMysqlConnectionFactory = FrontendMysqlConnectionFactory.INSTANCE;
    private final BackendMysqlConnectionFactory backendMysqlConnectionFactory = BackendMysqlConnectionFactory.INSTANCE;
    private final IDGenerator idGenerator = new IDGenerator();

    private MultiReactor multiReactor;
    private Connector connector;

    private ServerContext() {
    }

    public static ServerContext getInstance() {
        return INSTANCE;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public MysqlBackendConnectionPool getMysqlBackendConnectionPool() {
        return mysqlBackendConnectionPool;
    }

    public MultiReactor getMultiReactor() {
        return multiReactor;
    }

    public void setMultiReactor(MultiReactor multiReactor) {
        this.multiReactor = multiReactor;
    }

    public FrontendMysqlConnectionFactory getFrontendMysqlConnectionFactory() {
        return frontendMysqlConnectionFactory;
    }

    public BackendMysqlConnectionFactory getBackendMysqlConnectionFactory() {
        return backendMysqlConnectionFactory;
    }

    public IDGenerator getIdGenerator() {
        return idGenerator;
    }
}
