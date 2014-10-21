/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

/**
 * This class is responsible for reading resource traces from a file and creating a list of jobs.
 * <p>
 * <b>NOTE:</b>
 * <ul>
 * <li>This class can only take <tt>one</tt> trace file of the following format: <i>ASCII text, zip,
 * gz.</i>
 * <li>If you need to load multiple trace files, then you need to create multiple instances of this
 * class <tt>each with a unique 
 *      	 entity name</tt>.
 * <li>If size of the trace file is huge or contains lots of traces please increase the JVM heap
 * size accordingly by using <tt>java -Xmx</tt> option when running the simulation.
 * <li>The default job file size for sending to and receiving from a resource is
 * {@link gridsim.net.Link#DEFAULT_MTU}. However, you can specify the file size by using
 * {@link #setGridletFileSize(int)}.
 * <li>A job run time is only for 1 PE <tt>not</tt> the total number of allocated PEs. Therefore, a
 * Gridlet length is also calculated for 1 PE.<br>
 * For example, job #1 in the trace has a run time of 100 seconds for 2 processors. This means each
 * processor runs job #1 for 100 seconds, if the processors have the same specification.
 * </ul>
 * <p>
 * By default, this class follows the standard workload format as specified in <a
 * href="http://www.cs.huji.ac.il/labs/parallel/workload/">
 * http://www.cs.huji.ac.il/labs/parallel/workload/</a> <br>
 * However, you can use other format by calling the below methods before running the simulation:
 * <ul>
 * <li> {@link #setComment(String)}
 * <li> {@link #setField(int, int, int, int, int)}
 * </ul>
 * 
 * @author Anthony Sulistio and Marcos Dias de Assuncao
 * @since 5.0
 * 
 * @see Workload
 */
public class WorkloadFileReader implements WorkloadModel {

	private final File file; // file name

	private final int rating; // a PE rating

	private ArrayList<Cloudlet> jobs = null; // a list for getting all the

	// Gridlets

	// using Standard Workload Format
	private int JOB_NUM = 1 - 1; // job number

	private int SUBMIT_TIME = 2 - 1; // submit time of a Gridlet

	private final int RUN_TIME = 4 - 1; // running time of a Gridlet

	private final int NUM_PROC = 5 - 1; // number of processors needed for a

	// Gridlet
	private int REQ_NUM_PROC = 8 - 1; // required number of processors

	private int REQ_RUN_TIME = 9 - 1; // required running time

	private final int USER_ID = 12 - 1; // if of user who submitted the job

	private final int GROUP_ID = 13 - 1; // if of group of the user who

	// submitted the
	// job
	private int MAX_FIELD = 18; // max number of field in the trace file

	private String COMMENT = ";"; // a string that denotes the start of a

	// comment
	private static final int IRRELEVANT = -1; // irrelevant number

	private String[] fieldArray = null; // a temp array storing all the fields

	/**
	 * Create a new {@link WorkloadFileReader} object.
	 * 
	 * @param fileName the workload trace filename in one of the following format: <i>ASCII text,
	 *        zip, gz.</i>
	 * @param rating the resource's PE rating
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException This happens for the following conditions:
	 *         <ul>
	 *         <li>the workload trace file name is null or empty
	 *         <li>the resource PE rating <= 0
	 *         </ul>
	 * @pre fileName != null
	 * @pre rating > 0
	 * @post $none
	 */
	public WorkloadFileReader(final String fileName, final int rating) throws FileNotFoundException {
		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("Invalid trace file name.");
		} else if (rating <= 0) {
			throw new IllegalArgumentException("Resource PE rating must be > 0.");
		}

		file = new File(fileName);
		if (!file.exists()) {
			throw new FileNotFoundException("Workload trace " + fileName + " does not exist");
		}

		this.rating = rating;
	}

	/**
	 * Reads job information from a given file.
	 * 
	 * @return the list of gridlets read from the file; <code>null</code> in case of failure.
	 */
	@Override
	public ArrayList<Cloudlet> generateWorkload() {
		if (jobs == null) {
			jobs = new ArrayList<Cloudlet>();

			// create a temp array
			fieldArray = new String[MAX_FIELD];

			try {
				if (file.getName().endsWith(".gz")) {
					readGZIPFile(file);
				} else if (file.getName().endsWith(".zip")) {
					readZipFile(file);
				} else {
					readFile(file);
				}
			} catch (final FileNotFoundException e) {
			} catch (final IOException e) {
			}
		}

		return jobs;
	}

	/**
	 * Identifies the start of a comment line.
	 * 
	 * @param cmt a character that denotes the start of a comment, e.g. ";" or "#"
	 * @return <code>true</code> if it is successful, <code>false</code> otherwise
	 * @pre comment != null
	 * @post $none
	 */
	public boolean setComment(final String cmt) {
		boolean success = false;
		if (cmt != null && cmt.length() > 0) {
			COMMENT = cmt;
			success = true;
		}
		return success;
	}

	/**
	 * Tells this class what to look in the trace file. This method should be called before the
	 * start of the simulation.
	 * <p>
	 * By default, this class follows the standard workload format as specified in <a
	 * href="http://www.cs.huji.ac.il/labs/parallel/workload/">
	 * http://www.cs.huji.ac.il/labs/parallel/workload/</a> <br>
	 * However, you can use other format by calling this method.
	 * <p>
	 * The parameters must be a positive integer number starting from 1. A special case is where
	 * <tt>jobNum == -1</tt>, meaning the job or gridlet ID starts at 1.
	 * 
	 * @param maxField max. number of field/column in one row
	 * @param jobNum field/column number for locating the job ID
	 * @param submitTime field/column number for locating the job submit time
	 * @param runTime field/column number for locating the job run time
	 * @param numProc field/column number for locating the number of PEs required to run a job
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 * @throws IllegalArgumentException if any of the arguments are not within the acceptable ranges
	 * @pre maxField > 0
	 * @pre submitTime > 0
	 * @pre runTime > 0
	 * @pre numProc > 0
	 * @post $none
	 */
	public boolean setField(
			final int maxField,
			final int jobNum,
			final int submitTime,
			final int runTime,
			final int numProc) {
		// need to subtract by 1 since array starts at 0.
		if (jobNum > 0) {
			JOB_NUM = jobNum - 1;
		} else if (jobNum == 0) {
			throw new IllegalArgumentException("Invalid job number field.");
		} else {
			JOB_NUM = -1;
		}

		// get the max. number of field
		if (maxField > 0) {
			MAX_FIELD = maxField;
		} else {
			throw new IllegalArgumentException("Invalid max. number of field.");
		}

		// get the submit time field
		if (submitTime > 0) {
			SUBMIT_TIME = submitTime - 1;
		} else {
			throw new IllegalArgumentException("Invalid submit time field.");
		}

		// get the run time field
		if (runTime > 0) {
			REQ_RUN_TIME = runTime - 1;
		} else {
			throw new IllegalArgumentException("Invalid run time field.");
		}

		// get the number of processors field
		if (numProc > 0) {
			REQ_NUM_PROC = numProc - 1;
		} else {
			throw new IllegalArgumentException("Invalid number of processors field.");
		}

		return true;
	}

	// ------------------- PRIVATE METHODS -------------------

	/**
	 * Creates a Gridlet with the given information and adds to the list
	 * 
	 * @param id a Gridlet ID
	 * @param submitTime Gridlet's submit time
	 * @param runTime Gridlet's run time
	 * @param numProc number of processors
	 * @param reqRunTime user estimated run time
	 * @param userID user id
	 * @param groupID user's group id
	 * @pre id >= 0
	 * @pre submitTime >= 0
	 * @pre runTime >= 0
	 * @pre numProc > 0
	 * @post $none
	 */
	private void createJob(
			final int id,
			final long submitTime,
			final int runTime,
			final int numProc,
			final int reqRunTime,
			final int userID,
			final int groupID) {
		// create the cloudlet
		final int len = runTime * rating;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		final Cloudlet wgl = new Cloudlet(
				id,
				len,
				numProc,
				0,
				0,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		jobs.add(wgl);
	}

	/**
	 * Extracts relevant information from a given array
	 * 
	 * @param array an array of String
	 * @param line a line number
	 * @pre array != null
	 * @pre line > 0
	 */
	private void extractField(final String[] array, final int line) {
		try {
			Integer obj = null;

			// get the job number
			int id = 0;
			if (JOB_NUM == IRRELEVANT) {
				id = jobs.size() + 1;
			} else {
				obj = new Integer(array[JOB_NUM].trim());
				id = obj.intValue();
			}

			// get the submit time
			final Long l = new Long(array[SUBMIT_TIME].trim());
			final long submitTime = l.intValue();

			// get the user estimated run time
			obj = new Integer(array[REQ_RUN_TIME].trim());
			final int reqRunTime = obj.intValue();

			// if the required run time field is ignored, then use
			// the actual run time
			obj = new Integer(array[RUN_TIME].trim());
			int runTime = obj.intValue();

			final int userID = new Integer(array[USER_ID].trim()).intValue();
			final int groupID = new Integer(array[GROUP_ID].trim()).intValue();

			// according to the SWF manual, runtime of 0 is possible due
			// to rounding down. E.g. runtime is 0.4 seconds -> runtime = 0
			if (runTime <= 0) {
				runTime = 1; // change to 1 second
			}

			// get the number of allocated processors
			obj = new Integer(array[REQ_NUM_PROC].trim());
			int numProc = obj.intValue();

			// if the required num of allocated processors field is ignored
			// or zero, then use the actual field
			if (numProc == IRRELEVANT || numProc == 0) {
				obj = new Integer(array[NUM_PROC].trim());
				numProc = obj.intValue();
			}

			// finally, check if the num of PEs required is valid or not
			if (numProc <= 0) {
				numProc = 1;
			}
			createJob(id, submitTime, runTime, numProc, reqRunTime, userID, groupID);
		} catch (final Exception e) {

		}
	}

	/**
	 * Breaks a line of string into many fields.
	 * 
	 * @param line a line of string
	 * @param lineNum a line number
	 * @pre line != null
	 * @pre lineNum > 0
	 * @post $none
	 */
	private void parseValue(final String line, final int lineNum) {
		// skip a comment line
		if (line.startsWith(COMMENT)) {
			return;
		}

		final String[] sp = line.split("\\s+"); // split the fields based on a
		// space
		int len = 0; // length of a string
		int index = 0; // the index of an array

		// check for each field in the array
		for (final String elem : sp) {
			len = elem.length(); // get the length of a string

			// if it is empty then ignore
			if (len == 0) {
				continue;
			}
			fieldArray[index] = elem;
			index++;
		}

		if (index == MAX_FIELD) {
			extractField(fieldArray, lineNum);
		}
	}

	/**
	 * Reads a text file one line at the time
	 * 
	 * @param fl a file name
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 * @throws IOException if the there was any error reading the file
	 * @throws FileNotFoundException if the file was not found
	 */
	private boolean readFile(final File fl) throws IOException, FileNotFoundException {
		boolean success = false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));

			// read one line at the time
			int line = 1;
			String readLine = null;
			while (reader.ready() && (readLine = reader.readLine()) != null) {
				parseValue(readLine, line);
				line++;
			}

			reader.close();
			success = true;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return success;
	}

	/**
	 * Reads a gzip file one line at the time
	 * 
	 * @param fl a gzip file name
	 * @return <code>true</code> if successful; <code>false</code> otherwise.
	 * @throws IOException if the there was any error reading the file
	 * @throws FileNotFoundException if the file was not found
	 */
	private boolean readGZIPFile(final File fl) throws IOException, FileNotFoundException {
		boolean success = false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fl))));

			// read one line at the time
			int line = 1;                       
			String readLine = null;
                        while (reader.ready() && (readLine = reader.readLine()) != null) {
				parseValue(readLine, line);
				line++;
			}

			reader.close();
			success = true;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return success;
	}

	/**
	 * Reads a Zip file.
	 * 
	 * @param fl a zip file name
	 * @return <code>true</code> if reading a file is successful; <code>false</code> otherwise.
	 * @throws IOException if the there was any error reading the file
	 */
	private boolean readZipFile(final File fl) throws IOException {
		boolean success = false;
		ZipFile zipFile = null;
		try {
			BufferedReader reader = null;

			// ZipFile offers an Enumeration of all the files in the file
			zipFile = new ZipFile(fl);
			final Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				success = false; // reset the value again
				final ZipEntry zipEntry = e.nextElement();

				reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));

				// read one line at the time
				int line = 1;                       
				String readLine = null;
	                        while (reader.ready() && (readLine = reader.readLine()) != null)  {
					parseValue(readLine, line);
					line++;
				}

				reader.close();
				success = true;
			}
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}
		}

		return success;
	}
}
