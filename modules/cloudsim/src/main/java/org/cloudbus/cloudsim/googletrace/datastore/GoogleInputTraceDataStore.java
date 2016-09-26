package org.cloudbus.cloudsim.googletrace.datastore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.googletrace.GoogleTask;

public class GoogleInputTraceDataStore extends GoogleDataStore {

	public static String DATABASE_URL_PROP = "input_trace_database_url";
	public static String MIN_INTERESTED_TIME_PROP = "minimum_interested_time";
	public static String MAX_INTERESTED_TIME_PROP = "maximum_interested_time";
	
	private int nextTaskId = 0;
	
	private double minInterestedTime;
	private double maxInterestedTime;

	public GoogleInputTraceDataStore(Properties properties)
			throws ClassNotFoundException, SQLException {
		super(properties.getProperty(DATABASE_URL_PROP));

		if (isPropertySet(properties, MIN_INTERESTED_TIME_PROP) &&
				Double.parseDouble(properties.getProperty(MIN_INTERESTED_TIME_PROP)) < 0) {
			throw new IllegalArgumentException(MIN_INTERESTED_TIME_PROP + " must not be negative.");
		}

		if (isPropertySet(properties, MAX_INTERESTED_TIME_PROP) &&
				Double.parseDouble(properties.getProperty(MAX_INTERESTED_TIME_PROP)) < 0) {
			throw new IllegalArgumentException(MAX_INTERESTED_TIME_PROP + " must not be negative.");
		}

		minInterestedTime = isPropertySet(properties, MIN_INTERESTED_TIME_PROP) ? Double
				.parseDouble(properties.getProperty(MIN_INTERESTED_TIME_PROP)) : 0;

		maxInterestedTime = isPropertySet(properties, MAX_INTERESTED_TIME_PROP)? Double
				.parseDouble(properties.getProperty(MAX_INTERESTED_TIME_PROP)) : getMaxTraceTime() + 1;

	}

	private boolean isPropertySet(Properties properties, String propKey) {
		return properties.getProperty(propKey) != null;
	}

	protected double getMaxTraceTime() throws ClassNotFoundException,
			SQLException {
		Statement statement = null;
		Connection connection = null;

		try {
			Class.forName(DATASTORE_SQLITE_DRIVER);
			connection = getConnection();

			if (connection != null) {
				Log.printLine("Connected to the database: " + getDatabaseURL());

				statement = connection.createStatement();

				// getting the max submitTime from database
				ResultSet results = statement
						.executeQuery("SELECT MAX(submitTime) FROM tasks WHERE cpuReq > '0' AND memReq > '0'");

				while (results.next()) {
					return results.getDouble("MAX(submitTime)");
				}
			}
		} finally {
			close(statement, connection);
		}
		// It should never return this value.
		return -1;
	}

	public List<GoogleTask> getGoogleTaskInterval(int intervalIndex,
			double intervalSize) {

		if (intervalIndex < 0){
			throw new IllegalArgumentException("Interval index must be not negative");
		}

		if (intervalSize <= 0){
			throw new IllegalArgumentException("Interval size must be positive");
		}

		if (!hasMoreEvents(intervalIndex, intervalSize)) {
			Log.printLine(CloudSim.clock() + ": The Interval index is "
					+ intervalIndex
					+ " and there are not more events to be treated.");
			return null;
		}

		double minTime = Math.max(getMinInterestedTime(), (intervalIndex * intervalSize));
		double maxTime = Math.min(getMaxInterestedTime(), ((intervalIndex + 1) * intervalSize));

		Log.printLine(CloudSim.clock() + ": Interval index is " + intervalIndex
				+ ", minTime=" + minTime + " and maxTime=" + maxTime);
		
		List<GoogleTask> googleTasks = new ArrayList<GoogleTask>();

		Statement statement = null;
		Connection connection;
		try {
			connection = getConnection();

			if (connection != null) {
				statement = connection.createStatement();

				String sql = "SELECT submitTime, runtime, cpuReq, memReq, priority FROM tasks WHERE cpuReq > '0' AND memReq > '0' AND submitTime >= '"
						+ minTime + "' AND submitTime < '" + maxTime + "'";

				ResultSet results = statement.executeQuery(sql);

				while (results.next()) {
					/* 
					 * TODO We need to check if this nextTaskId variable is considered for post processing.
					 * If we execute part of the trace more than one time, the same task can be have different 
					 * taskId (depending of the interval size).  
					 */
					nextTaskId++;
					GoogleTask task = new GoogleTask(nextTaskId,
							results.getDouble("submitTime"),
							results.getDouble("runtime"),
							results.getDouble("cpuReq"),
							results.getDouble("memReq"),
							convertPriorityToPriorityClass(results.getInt("priority")));
					
					googleTasks.add(task);
				}
				
				Log.printLine(CloudSim.clock() + ": Interval index is " + intervalIndex + " and number of tasks is " + googleTasks.size());
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return googleTasks;
	}

	private int convertPriorityToPriorityClass(int priority) {
		if(priority <= 1) {
			return 2;
		} else if (priority <= 8) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getMinInterestedTime() {
		return minInterestedTime;
	}

	public double getMaxInterestedTime() {
		return maxInterestedTime;
	}

	public boolean hasMoreEvents(int intervalIndex,
			double intervalSize) {
		return (intervalIndex >= 0 && (intervalIndex * intervalSize) <= getMaxInterestedTime());
	}
}
