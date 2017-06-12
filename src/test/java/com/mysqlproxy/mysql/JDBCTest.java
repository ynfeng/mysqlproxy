package com.mysqlproxy.mysql;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCTest {

    @Test
    public void jdbcTest(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url ="jdbc:mysql://127.0.0.1:3306/ynfeng" ;//"jdbc:mysql://10.211.55.5:3306/ynfeng";
            String username = "root";
            String password = "123456";
            Connection con = DriverManager.getConnection(url, username, password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select  * from t_ynfeng");
            while(rs.next()){
                System.out.println(rs.getString(2));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
