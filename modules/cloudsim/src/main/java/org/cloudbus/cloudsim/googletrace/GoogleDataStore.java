package org.cloudbus.cloudsim.googletrace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.cloudbus.cloudsim.Log;

public class GoogleDataStore {

	protected static final String DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";

	private String databaseURL;

	public GoogleDataStore(String databaseURL) {
		setDatabaseURL(databaseURL);
		
	}
	
	protected Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(getDatabaseURL());
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Error while getting a connection to " + getDatabaseURL());
			throw e;
		}
	}
	
	protected void close(Statement statement, Connection conn) {
		if (statement != null) {
			try {
				if (!statement.isClosed()) {
					statement.close();
				}
			} catch (SQLException e) {
				Log.print(e);
				Log.printLine("Couldn't close statement.");
			}
		}

		if (conn != null) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				Log.print(e);
				Log.printLine("Couldn't close connection.");
			}
		}
	}

	protected String getDatabaseURL() {
		return databaseURL;
	}

	protected void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}
}
