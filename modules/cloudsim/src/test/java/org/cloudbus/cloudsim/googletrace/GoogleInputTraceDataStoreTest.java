package org.cloudbus.cloudsim.googletrace;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GoogleInputTraceDataStoreTest {

	private final double ACCEPTABLE_DIFFERENCE = 0.00001;
	private final static int NUMBER_OF_TASKS = 100;
	
	private static String databaseFile = "inputTraceTest.sqlite3";
    private static String databaseURL = "jdbc:sqlite:" + databaseFile;

	private static double DEFAULT_RUNTIME = 1000;
	private static Properties properties;
	
	@BeforeClass
	public static void setUp() throws ClassNotFoundException, SQLException {
		createAndPopulateTestDatabase();
	}

	@Before
	public void init() {
		properties = new Properties();
		properties.setProperty(GoogleInputTraceDataStore.DATABASE_URL_PROP, databaseURL);
		
	}
		
	private static void createAndPopulateTestDatabase() throws ClassNotFoundException, SQLException {
		Class.forName(GoogleDataStore.DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseURL);

		if (connection != null) {
			// Creating the database
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS tasks("
					+ "submitTime REAL, "
					+ "jid REAL, " 
					+ "tid INTEGER, "
					+ "user TEXT, " 
					+ "schedulingClass INTEGER, "
					+ "priority INTEGER, " 
					+ "runtime REAL, "
					+ "endTime REAL, " 
					+ "cpuReq REAL, " 
					+ "memReq REAL, "
					+ "userClass TEXT" + ")");
			statement.close();

			String INSERT_CLOUDLET_SQL = "INSERT INTO tasks"
					+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			// populating the database
			for (int i = 1; i <= NUMBER_OF_TASKS; i++) {
				PreparedStatement insertMemberStatement = connection
						.prepareStatement(INSERT_CLOUDLET_SQL);
				insertMemberStatement.setDouble(1, getTimeInMicro(i));
				insertMemberStatement.setDouble(2, -1); // jid is not important for now
				insertMemberStatement.setInt(3, -1); // tid is not important for now
				insertMemberStatement.setNString(4, "user"); // user is not important for now
				insertMemberStatement.setInt(5, -1); // scheduling class is not important for now
				insertMemberStatement.setInt(6, -1); // priority is not important for now
				insertMemberStatement.setDouble(7, DEFAULT_RUNTIME);
				insertMemberStatement.setDouble(8, i + DEFAULT_RUNTIME);
				insertMemberStatement.setDouble(9, 1); // cpuReq is not important for now
				insertMemberStatement.setDouble(10, 1); // memReq is not important for now
				insertMemberStatement.setNString(11, "userClass"); // userClass is not important for now
				insertMemberStatement.execute();
			}
			connection.close();
		}
	}

	@AfterClass
	public static void tearDown() {
		new File(databaseFile).delete();
	}
	
	@Test
	public void testGetMaxSubmitTime() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(properties);
		Assert.assertEquals(0, inputTrace.getMinInterestedTime(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(getTimeInMicro(NUMBER_OF_TASKS), inputTrace.getMaxTraceTime(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(getTimeInMicro(NUMBER_OF_TASKS) + 1, inputTrace.getMaxInterestedTime(), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testGetGTask1MicroInterval() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		for (int i = 1; i <= NUMBER_OF_TASKS; i++) {
			Assert.assertEquals(1,
					inputTrace.getGoogleTaskInterval(i, getTimeInMicro(1)).size());
		}
	}
	
	@Test
	public void testGetGTaskBiggerMicroInterval() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		
		// because the submitTime started from 1 instead of 0
		Assert.assertEquals(NUMBER_OF_TASKS/2 - 1, 
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS/2)).size());
		Assert.assertEquals(NUMBER_OF_TASKS/2,
				inputTrace.getGoogleTaskInterval(1, getTimeInMicro(NUMBER_OF_TASKS/2)).size());
		
		// because the minTime of this interval is exactly the maximum time of the fake trace
		Assert.assertEquals(1,
				inputTrace.getGoogleTaskInterval(2, getTimeInMicro(NUMBER_OF_TASKS/2)).size());
		
		// because the minTime of this interval is bigger than maximum time of the fake trace 
		Assert.assertNull(inputTrace.getGoogleTaskInterval(3, getTimeInMicro(NUMBER_OF_TASKS/2)));
	}
	
	@Test
	public void testGetGTaskInvalidInterval() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		Assert.assertNull(inputTrace.getGoogleTaskInterval(NUMBER_OF_TASKS + 1,
				getTimeInMicro(1)));
	}
	
	@Test
	public void testGetGTaskInvalidInterval2() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		Assert.assertNull(inputTrace.getGoogleTaskInterval(-1,
				getTimeInMicro(1)));
	}
	
	@Test
	public void testGetGTaskIntervalWithMinValue() throws Exception {
		double min = getTimeInMicro(51);
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		
		Assert.assertEquals(NUMBER_OF_TASKS/2,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());
		
	}

	@Test
	public void testGetGTaskIntervalWithMaxValue() throws Exception {
		double max = getTimeInMicro(51);
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		
		Assert.assertEquals(NUMBER_OF_TASKS/2,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());
		
	}
	
	@Test
	public void testGetGTaskIntervalWithMinMaxValues() throws Exception {
		double min = getTimeInMicro(20);
		double max = getTimeInMicro(80);
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);
		
		Assert.assertEquals(80 - 20,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());
		
	}
	
	private static double getTimeInMicro(double timeInMinutes) {
		return timeInMinutes * 60 * 1000000;
	}
	
}
