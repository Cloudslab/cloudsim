package org.cloudbus.cloudsim.googletrace.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.googletrace.GoogleTaskState;

public class GoogleOutputTaskDataStore extends GoogleDataStore {

	public static final String DATABASE_URL_PROP = "output_tasks_database_url";
	
	private static final String GOOGLE_TASK_TABLE_NAME = "googletask";
			
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
	
	private static final String INSERT_TASK_SQL = "INSERT INTO " + GOOGLE_TASK_TABLE_NAME
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	
	public void addTask(GoogleTaskState taskState) {
		if (taskState == null) {
			Log.printLine("Cloudlet must no be null.");
			return;
		}
		
		Log.printLine("Adding vm #" + taskState.getTaskId() + " into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;
		
		try {
			connection = getConnection();			
			insertMemberStatement = connection.prepareStatement(INSERT_TASK_SQL);		
			insertMemberStatement.setInt(1, taskState.getTaskId());
			insertMemberStatement.setInt(2, taskState.getResourceId());
			insertMemberStatement.setDouble(3, taskState.getCpuReq());
			insertMemberStatement.setDouble(4, taskState.getSubmitTime());
			insertMemberStatement.setDouble(5, taskState.getStartTime());
			insertMemberStatement.setDouble(6, taskState.getFinishTime());
			insertMemberStatement.setDouble(7, taskState.getRuntime());
			insertMemberStatement.setInt(8, taskState.getStatus());
			insertMemberStatement.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
			Log.printLine("Couldn't add cloudlet into database.");			
		} finally {
			close(insertMemberStatement, connection);
		}
	}
	
	private static final String SELECT_ALL_TASKS_SQL = "SELECT * FROM " + GOOGLE_TASK_TABLE_NAME;

	public List<GoogleTaskState> getAllTasks() {
		Statement statement = null;
		Connection conn = null;
		List<GoogleTaskState> taskStates = new ArrayList<GoogleTaskState>();
		
		try {
			conn = getConnection();
			statement = conn.createStatement();

			statement.execute(SELECT_ALL_TASKS_SQL);
			ResultSet rs = statement.getResultSet();

			while (rs.next()) {
				taskStates.add(new GoogleTaskState(rs
						.getInt("cloudlet_id"), rs.getInt("resource_id"), rs
						.getDouble("cpu_req"), rs.getDouble("submit_time"), rs
						.getDouble("start_time"), rs.getDouble("finish_time"),
						rs.getDouble("runtime"), rs.getInt("status")));
			}
			return taskStates;
		} catch (SQLException e) {
			Log.print(e);
			Log.printLine("Couldn't get cloudlets from DB.");
			return null;
		}
	}

	public boolean addTaskList(List<GoogleTaskState> taskStates) {
		if (taskStates == null) {
			Log.printLine("taskStates must no be null.");
			return false;
		}		
		Log.printLine("Adding " + taskStates.size() + " VMs into database.");
		
		PreparedStatement insertMemberStatement = null;
		
		Connection connection = null;

		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			insertMemberStatement = connection.prepareStatement(INSERT_TASK_SQL);
			insertMemberStatement = connection
					.prepareStatement(INSERT_TASK_SQL);
		
			for (GoogleTaskState taskState : taskStates) {
				insertMemberStatement.setInt(1, taskState.getTaskId());
				insertMemberStatement.setInt(2, taskState.getResourceId());
				insertMemberStatement.setDouble(3, taskState.getCpuReq());
				insertMemberStatement.setDouble(4, taskState.getSubmitTime());
				insertMemberStatement.setDouble(5, taskState.getStartTime());
				insertMemberStatement.setDouble(6, taskState.getFinishTime());
				insertMemberStatement.setDouble(7, taskState.getRuntime());
				insertMemberStatement.setInt(8, taskState.getStatus());
				insertMemberStatement.addBatch();
			}
			
			int[] executeBatch = insertMemberStatement.executeBatch();
			for (int i : executeBatch) {
				if (i == PreparedStatement.EXECUTE_FAILED) {
					Log.printLine("Rollback will be executed.");
					connection.rollback();
					return false;
				}
			}

			connection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Log.printLine("Couldn't add tasks.");
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				Log.printLine("Couldn't rollback transaction.");
			}
			return false;
		} finally {
			close(insertMemberStatement, connection);
		}
	}
}
