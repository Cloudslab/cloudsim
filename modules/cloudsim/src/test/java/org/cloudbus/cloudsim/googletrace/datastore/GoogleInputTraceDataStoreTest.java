package org.cloudbus.cloudsim.googletrace.datastore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.Assert;

import org.cloudbus.cloudsim.googletrace.datastore.GoogleDataStore;
import org.cloudbus.cloudsim.googletrace.datastore.GoogleInputTraceDataStore;
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
				insertMemberStatement.setDouble(1, getTimeInMicro(i)); // submit time
				insertMemberStatement.setDouble(2, -1); // jid is not important for now
				insertMemberStatement.setInt(3, -1); // tid is not important for now
				insertMemberStatement.setNString(4, "user"); // user is not important for now
				insertMemberStatement.setInt(5, -1); // scheduling class is not important for now
				insertMemberStatement.setInt(6, -1); // priority is not important for now
				insertMemberStatement.setDouble(7, DEFAULT_RUNTIME); // runtime
				insertMemberStatement.setDouble(8, i + DEFAULT_RUNTIME); // endtime
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
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(properties); // initialize a trace
		Assert.assertEquals(0, inputTrace.getMinInterestedTime(), ACCEPTABLE_DIFFERENCE); // test default minInterestTime
		Assert.assertEquals(getTimeInMicro(NUMBER_OF_TASKS), inputTrace.getMaxTraceTime(), ACCEPTABLE_DIFFERENCE); // test default maxTraceTime
		Assert.assertEquals(getTimeInMicro(NUMBER_OF_TASKS) + 1, inputTrace.getMaxInterestedTime(), ACCEPTABLE_DIFFERENCE);// test default maxInterestTime
		// maxTraceTime = 6.0E9
//		System.out.println(6.0E9/60000000);
//		System.out.println(getTimeInMicro(100));

	}

	@Test
	public void testSetValuesofInterestTime() throws Exception{
		double newMax = getTimeInMicro(60);
		double newMin = getTimeInMicro(20);
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(newMax));
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(newMin));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(properties);
		Assert.assertEquals(getTimeInMicro(60), inputTrace.getMaxInterestedTime(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(getTimeInMicro(20), inputTrace.getMinInterestedTime(), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testGetGTask1MicroInterval() throws Exception {
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);


		for (int i = 1; i <= NUMBER_OF_TASKS; i++) {
			Assert.assertEquals(1,
					inputTrace.getGoogleTaskInterval(i, getTimeInMicro(1)).size()); // test if tasks are divided in groups of one
		}

		// test if interval index equals to 0 is empty
		Assert.assertEquals(0,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(1)).size());

		// test limits of intervals, using IntervalSize equals to 2
		Assert.assertEquals(1, inputTrace.getGoogleTaskInterval(0, getTimeInMicro(2)).size());
		Assert.assertEquals(1, inputTrace.getGoogleTaskInterval(NUMBER_OF_TASKS/2, getTimeInMicro(2)).size());


		// test all sizes of intervals in pairs without limits
		for (int i = 1; i < NUMBER_OF_TASKS/2; i++) {
			Assert.assertEquals(2,
					inputTrace.getGoogleTaskInterval(i, getTimeInMicro(2)).size()); // test if tasks are divided in groups of one
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
	public void testSizeListofGetGTaskInterval() throws Exception{
		// test limit values in intervals index (0, 100 and variations)
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

		// test the number of tasks between time 51 and 100 (although the Interval size is greater than maxSubmitTime)
		Assert.assertEquals(NUMBER_OF_TASKS/2,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());

		
	}

	@Test
	public void testGetGTaskIntervalWithMaxValue() throws Exception {
		double max = getTimeInMicro(NUMBER_OF_TASKS/2) + 1;
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);

		// test the number of tasks between time 0 and 50 (although the Interval size is greater than maxSubmitTime)
		Assert.assertEquals(NUMBER_OF_TASKS/2,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());


	}

	@Test
	public void testGetGTaskIntervalWithMinValueNegative() throws Exception{
		double min = getTimeInMicro(-1);
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);

		// test the number of tasks between time 0 and 100 (because -1 is not considered)
		Assert.assertEquals(NUMBER_OF_TASKS,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());
	}

	@Test
	public void testGetGTaskIntervalWithMaxValueGreaterThanMaxSubmitTime() throws Exception {
		double max = getTimeInMicro(NUMBER_OF_TASKS + 1);
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);

		// test the number of tasks between time 0 and 100 (Because 101 is not considered)
		Assert.assertEquals(NUMBER_OF_TASKS,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());

		// remember to test with max time greater than max submit time
		
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

		// remember to test limit values (eg. -1 and 0, 100 and 101, 0 and 0, 100 and 100)

		// testing limit values
		min = getTimeInMicro(-1);
		max = getTimeInMicro(0);
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		inputTrace = new GoogleInputTraceDataStore(
				properties);

		Assert.assertEquals(0,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());

		min = getTimeInMicro(NUMBER_OF_TASKS);
		max = getTimeInMicro(NUMBER_OF_TASKS + 1); // adding 1 minute
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		inputTrace = new GoogleInputTraceDataStore(
				properties);

		Assert.assertEquals(1,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());


		min = getTimeInMicro(0);
		max = getTimeInMicro(0);
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		inputTrace = new GoogleInputTraceDataStore(
				properties);

		Assert.assertEquals(0,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());


		min = getTimeInMicro(NUMBER_OF_TASKS);
		max = getTimeInMicro(NUMBER_OF_TASKS) + 1; // adding 1 microsecond
		properties.setProperty(GoogleInputTraceDataStore.MIN_INTERESTED_TIME_PROP, String.valueOf(min));
		properties.setProperty(GoogleInputTraceDataStore.MAX_INTERESTED_TIME_PROP, String.valueOf(max));
		inputTrace = new GoogleInputTraceDataStore(
				properties);

		Assert.assertEquals(1,
				inputTrace.getGoogleTaskInterval(0, getTimeInMicro(NUMBER_OF_TASKS + 1)).size());

	}

	@Test
	public void testHasMoreEvents() throws Exception{
		GoogleInputTraceDataStore inputTrace = new GoogleInputTraceDataStore(
				properties);

		for (int i = -2; i <= NUMBER_OF_TASKS + 1; i++) {

			if (i >= 101 || i < 0){
				Assert.assertFalse(inputTrace.hasMoreEvents(i, getTimeInMicro(1)));
			} else {
				Assert.assertTrue(inputTrace.hasMoreEvents(i, getTimeInMicro(1)));
			}
		}

	}

	@Test
	public void testGTaskIntervalEqualsList() throws Exception{
		// compare the return of GoogleTaskInterval with expected list
	}
	
	private static double getTimeInMicro(double timeInMinutes) {
		return timeInMinutes * 60 * 1000000;
	}
	
}
