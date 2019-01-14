package com.iig.gcp.extraction.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.stereotype.Component;
 
@Component
public class ConnectionUtils {
 
    public static Connection getConnection() 
              throws Exception {
         return OracleConnUtils.getOracleConnection();
    }
    
 
     
    public static void closeQuietly(Connection conn) {
        try {
        	conn.commit();
            conn.close();
        } catch (Exception e) {
        }
    }
 
    public static void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
        	rs.close();
        } catch (Exception e) {
        }
    }
    
    public static void closePrepareStatement(PreparedStatement ps) {
        try {
        	ps.close();
        } catch (Exception e) {
        }
    }

}
