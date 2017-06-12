package com.mysqlproxy.mysql;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

public class JDBCTest {

    @Test
    public void jdbcTest(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url ="jdbc:mysql://10.211.55.5:3306/ynfeng" ;//"jdbc:mysql://10.211.55.5:3306/ynfeng";
            String username = "root";
            String password = "123456";
            Connection con = DriverManager.getConnection(url, username, password);
            System.out.println();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
