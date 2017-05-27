package com.mysqlproxy.net;

import com.mysqlproxy.mysql.Connection;
import com.mysqlproxy.mysql.MysqlConnection;

import java.io.IOException;

/**
 * Created by ynfeng on 2017/5/12.
 */
public class MultiReactor {
    private Reactor[] reactors = new Reactor[Runtime.getRuntime().availableProcessors()];
    private int index = 0;

    public MultiReactor() throws IOException {
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
        if (index >= reactors.length) {
            index = 0;
        } else {
            index++;
        }
        reactors[index].register(mysqlConnection);
    }
}
