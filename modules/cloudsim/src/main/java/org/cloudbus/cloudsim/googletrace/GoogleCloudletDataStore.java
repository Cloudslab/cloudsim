package org.cloudbus.cloudsim.googletrace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;

public class GoogleCloudletDataStore {

	public static final String ACCOUNTING_DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";

	private static final String CLOUDLET_TABLE_NAME = "cloudlet";
			
	private String databaseURL;
	
	public GoogleCloudletDataStore(String databaseURL) {
		setDatabaseURL(databaseURL);

		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("Cloudlet database URL received is " + databaseURL);

			Class.forName(ACCOUNTING_DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS cloudlet("
							+ "cloudlet_id INTEGER NOT NULL, "
							+ "resource_id INTEGER, "
							+ "cpu_req REAL, "
							+ "submit_time REAL, "
							+ "start_time REAL, "
							+ "finish_time REAL, "
							+ "runtime REAL, "
							+ "status INTEGER, "							
							+ "PRIMARY KEY (cloudlet_id)"
							+ ")");
		} catch (Exception e) {
			Log.print(e);
			Log.printLine("Error while initializing the Cloudlet database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_CLOUDLET_SQL = "INSERT INTO " + CLOUDLET_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

	public void addCloudlet(GoogleCloudlet cloudlet) {
		if (cloudlet == null) {
			Log.printLine("Cloudlet must no be null.");
			return;
		}
		
		Log.printLine("Adding cloudlet #" + cloudlet.getCloudletId() + " into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
				

		try {
			connection = getConnection();			
			insertMemberStatement = connection.prepareStatement(INSERT_CLOUDLET_SQL);		
			insertMemberStatement.setInt(1, cloudlet.getCloudletId());
			insertMemberStatement.setInt(2, cloudlet.getResourceId());
			insertMemberStatement.setDouble(3, cloudlet.getCpuReq());
			insertMemberStatement.setDouble(4, cloudlet.getDelay());
			insertMemberStatement.setDouble(5, cloudlet.getExecStartTime());
			insertMemberStatement.setDouble(6, cloudlet.getFinishTime());
			insertMemberStatement.setDouble(7, cloudlet.getActualCPUTime());
			insertMemberStatement.setInt(8, cloudlet.getStatus());
			insertMemberStatement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			Log.printLine("Couldn't add cloudlet into database.");			
		} finally {
			close(insertMemberStatement, connection);
		}
	}
	
	private static final String SELECT_ALL_CLOUDLETS_SQL = "SELECT * FROM " + CLOUDLET_TABLE_NAME;

	public List<GoogleCloudletState> getAllCloudlets() {
		Statement statement = null;
		Connection conn = null;
		List<GoogleCloudletState> cloudletStates = new ArrayList<GoogleCloudletState>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_ALL_CLOUDLETS_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				cloudletStates.add(new GoogleCloudletState(rs
						.getInt("cloudlet_id"), rs.getInt("resource_id"), rs
						.getDouble("cpu_req"), rs.getDouble("submit_time"), rs
						.getDouble("start_time"), rs.getDouble("finish_time"),
						rs.getDouble("runtime"), rs.getInt("status")));
			}
			return cloudletStates;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get cloudlets from DB.");
			return null;
		}
	}
	
	/**
	 * @return the connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(getDatabaseURL());
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Error while getting a connection to " + getDatabaseURL());
			throw e;
		}
	}
	
	private void close(Statement statement, Connection conn) {
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
