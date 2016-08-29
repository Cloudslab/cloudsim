package org.cloudbus.cloudsim.googletrace;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

public class GoogleInputTraceDataStore extends GoogleDataStore {

	double maximunSubmitTime;

	public GoogleInputTraceDataStore(String inputTraceDatabaseURL) {
		super(inputTraceDatabaseURL);

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
					maximunSubmitTime = results.getDouble("MAX(submitTime)");
					Log.printLine("The maximum submitTime of trace is " + getMaximunSubmitTime());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error while initializing the Input Trace database store.");
		} finally {
			close(statement, connection);
		}
	}

	public double getMaximunSubmitTime() {
		return maximunSubmitTime;
	}

	public List<GoogleTask> getGoogleTaskInterval(int intervalIndex,
			int intervalSize) {
			
		double intervalSizeInMicro = intervalSize * 60 * 1000000; 
		double minTime = intervalIndex * intervalSizeInMicro;
		double maxTime = (intervalSize + 1) * intervalSizeInMicro;		
		
		if (minTime > getMaximunSubmitTime()) {
			return null;
		}
		
		List<GoogleTask> googleTasks = new ArrayList<GoogleTask>();

		Statement statement = null;
		Connection connection;
		try {
			connection = getConnection();

			if (connection != null) {
				Log.printLine("Connected to the database");
				statement = connection.createStatement();

				ResultSet results = statement
						.executeQuery("SELECT submitTime, runtime, cpuReq, memReq FROM tasks WHERE cpuReq > '0' AND memReq > '0' AND submitTime >= '"
								+ minTime
								+ "' AND submitTime < '"
								+ maxTime
								+ "'");

//				ResultSet results = statement
//						.executeQuery("SELECT submitTime, runtime, cpuReq, memReq FROM tasks WHERE cpuReq > '0' AND memReq > '0' AND submitTime > '"
//								+ minTime
//								+ "' AND submitTime <= '"
//								+ maxTime
//								+ "'");
				
				int count = 0;
				while (results.next()) {
					count++;
//					System.out.println(results.getDouble("submitTime")
//							+ ", "
//							// + results.getDouble("jid") + ", "
//							// + results.getInt("tid") + ", "
//							// + results.getString("user") + ", "
//							// + results.getInt("schedulingClass") + ", "
//							// + results.getInt("priority") + ", "
//							+ (results.getDouble("runtime")) + ","
//							// + results.getDouble("endTime") + ", "
//							+ results.getDouble("cpuReq") + ", "
//							+ results.getDouble("memReq"));// + ", "
//					// + results.getString("userClass"));

					GoogleTask task = new GoogleTask(count,
							results.getDouble("submitTime"),
							results.getDouble("runtime"),
							results.getDouble("cpuReq"),
							results.getDouble("memReq"));

					googleTasks.add(task);
				}
				
				System.out.println(CloudSim.clock() + " - Interval " + intervalIndex + " and number of tasks is " + googleTasks.size());
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return googleTasks;
	}
	
}
