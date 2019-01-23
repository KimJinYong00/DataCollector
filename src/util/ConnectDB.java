package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectDB {
	final static String ip = PropReader.getInstance().getProperty("dbIP");
	final static String id = PropReader.getInstance().getProperty("dbUser");
	final static String pw = PropReader.getInstance().getProperty("dbPassword");
	public Connection con = null;
	public ResultSet rs;
	public Statement stmt = null;
	
	public ConnectDB() {
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(ip, id, pw);
			stmt = (Statement) con.createStatement();
			rs = stmt.executeQuery("use mysql");

		} catch (Exception e) {

		}
	}
}
