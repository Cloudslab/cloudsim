package org.cloudbus.cloudsim.googletrace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;

public class GoogleOutputTaskDataStore extends GoogleDataStore {

	public static final String DATABASE_URL_PROP = "output_tasks_database_url";
	
	private static final String CLOUDLET_TABLE_NAME = "googletask";
			
	public GoogleOutputTaskDataStore(Properties properties) {
		super(properties.getProperty(DATABASE_URL_PROP));

		Statement statement = null;
		Connection connection = null;
		try {
			Log.printLine("output_tasks_database_url=" + getDatabaseURL());

			Class.forName(DATASTORE_SQLITE_DRIVER);

			connection = getConnection();
			statement = connection.createStatement();
			statement
					.execute("CREATE TABLE IF NOT EXISTS googletask("
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
			e.printStackTrace();
			Log.printLine("Error while initializing the GoogleTask database store.");
		} finally {
			close(statement, connection);
		}
	}
	
	private static final String INSERT_CLOUDLET_SQL = "INSERT INTO " + CLOUDLET_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

	public void addCloudlet(GoogleTask cloudlet) {
		if (cloudlet == null) {
			Log.printLine("Cloudlet must no be null.");
			return;
		}
		
		Log.printLine("Adding cloudlet #" + cloudlet.getId() + " into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
				

		try {
			connection = getConnection();			
			insertMemberStatement = connection.prepareStatement(INSERT_CLOUDLET_SQL);		
			insertMemberStatement.setInt(1, cloudlet.getId());
			insertMemberStatement.setInt(2, -1);
//			insertMemberStatement.setInt(2, cloudlet.getResourceId());
			insertMemberStatement.setDouble(3, cloudlet.getCpuReq());
			insertMemberStatement.setDouble(4, cloudlet.getSubmitTime());
			insertMemberStatement.setDouble(5, cloudlet.getStartTime());
			insertMemberStatement.setDouble(6, cloudlet.getFinishTime());
//			insertMemberStatement.setDouble(7, cloudlet.getActualCPUTime());
			insertMemberStatement.setDouble(7, -1);
//			insertMemberStatement.setInt(8, cloudlet.getStatus());
			insertMemberStatement.setInt(8, Cloudlet.SUCCESS);
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
	
	public void addCloudlet(GoogleCloudletState cloudlet) {
		if (cloudlet == null) {
			Log.printLine("Cloudlet must no be null.");
			return;
		}
		
		Log.printLine("Adding vm #" + cloudlet.getCloudletId() + " into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
				

		try {
			connection = getConnection();			
			insertMemberStatement = connection.prepareStatement(INSERT_CLOUDLET_SQL);		
			insertMemberStatement.setInt(1, cloudlet.getCloudletId());
			insertMemberStatement.setInt(2, cloudlet.getResourceId());
			insertMemberStatement.setDouble(3, cloudlet.getCpuReq());
			insertMemberStatement.setDouble(4, cloudlet.getSubmitTime());
			insertMemberStatement.setDouble(5, cloudlet.getStartTime());
			insertMemberStatement.setDouble(6, cloudlet.getFinishTime());
			insertMemberStatement.setDouble(7, cloudlet.getRuntime());
			insertMemberStatement.setInt(8, cloudlet.getStatus());
			insertMemberStatement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			Log.printLine("Couldn't add cloudlet into database.");			
		} finally {
			close(insertMemberStatement, connection);
		}
	}
}
