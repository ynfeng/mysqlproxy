package com.mysqlproxy.net;

import com.mysqlproxy.mysql.MysqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class MultiReactor {
    private Logger logger = LoggerFactory.getLogger(MultiReactor.class);
    private Reactor[] reactors = new Reactor[Runtime.getRuntime().availableProcessors()];
    private int index = 0;

    public MultiReactor() throws IOException {
        logger.info("初始化{}个reactor",reactors.length);
        for (int i = 0; i < reactors.length; i++) {
            reactors[i] = new Reactor();
        }
    }

    public void startup() {
        for (int i = 0; i < reactors.length; i++) {
            reactors[i].startup();
        }
    }

    public void postRegister(MysqlConnection mysqlConnection) {
        if (index >= reactors.length - 1) {
            index = 0;
        } else {
            index++;
        }
        logger.debug("向第{}个reactor中注册新连接",index);
        reactors[index].register(mysqlConnection);
    }
}
