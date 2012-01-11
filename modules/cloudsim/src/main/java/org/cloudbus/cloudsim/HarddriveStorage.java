/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

/**
 * An implementation of a storage system. It simulates the behaviour of a typical harddrive storage.
 * The default values for this storage are those of a Maxtor DiamonMax 10 ATA harddisk with the
 * following parameters:
 * <ul>
 * <li>latency = 4.17 ms
 * <li>avg seek time = 9 ms
 * <li>max transfer rate = 133 MB/sec
 * </ul>
 * 
 * @author Uros Cibej
 * @author Anthony Sulistio
 * @since CloudSim Toolkit 1.0
 */
public class HarddriveStorage implements Storage {

	/** a list storing the names of all the files on the harddrive. */
	private List<String> nameList;

	/** a list storing all the files stored on the harddrive. */
	private List<File> fileList;

	/** the name of the harddrive. */
	private final String name;

	/** a generator required to randomize the seek time. */
	private ContinuousDistribution gen;

	/** the current size of files on the harddrive. */
	private double currentSize;

	/** the total capacity of the harddrive in MB. */
	private final double capacity;

	/** the maximum transfer rate in MB/sec. */
	private double maxTransferRate;

	/** the latency of the harddrive in seconds. */
	private double latency;

	/** the average seek time in seconds. */
	private double avgSeekTime;

	/**
	 * Creates a new harddrive storage with a given name and capacity.
	 * 
	 * @param name the name of the new harddrive storage
	 * @param capacity the capacity in MByte
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public HarddriveStorage(String name, double capacity) throws ParameterException {
		if (name == null || name.length() == 0) {
			throw new ParameterException("HarddriveStorage(): Error - invalid storage name.");
		}

		if (capacity <= 0) {
			throw new ParameterException("HarddriveStorage(): Error - capacity <= 0.");
		}

		this.name = name;
		this.capacity = capacity;
		init();
	}

	/**
	 * Creates a new harddrive storage with a given capacity. In this case the name of the storage
	 * is a default name.
	 * 
	 * @param capacity the capacity in MByte
	 * @throws ParameterException when the capacity is not valid
	 */
	public HarddriveStorage(double capacity) throws ParameterException {
		if (capacity <= 0) {
			throw new ParameterException("HarddriveStorage(): Error - capacity <= 0.");
		}
		name = "HarddriveStorage";
		this.capacity = capacity;
		init();
	}

	/**
	 * The initialization of the harddrive is done in this method. The most common parameters, such
	 * as latency, average seek time and maximum transfer rate are set. The default values are set
	 * to simulate the Maxtor DiamonMax 10 ATA harddisk. Furthermore, the necessary lists are
	 * created.
	 */
	private void init() {
		fileList = new ArrayList<File>();
		nameList = new ArrayList<String>();
		gen = null;
		currentSize = 0;

		latency = 0.00417;     // 4.17 ms in seconds
		avgSeekTime = 0.009;   // 9 ms
		maxTransferRate = 133; // in MB/sec
	}

	/**
	 * Gets the available space on this storage in MB.
	 * 
	 * @return the available space in MB
	 */
	@Override
	public double getAvailableSpace() {
		return capacity - currentSize;
	}

	/**
	 * Checks if the storage is full or not.
	 * 
	 * @return <tt>true</tt> if the storage is full, <tt>false</tt> otherwise
	 */
	@Override
	public boolean isFull() {
		if (Math.abs(currentSize - capacity) < .0000001) { // currentSize == capacity
			return true;
		}
		return false;
	}

	/**
	 * Gets the number of files stored on this storage.
	 * 
	 * @return the number of stored files
	 */
	@Override
	public int getNumStoredFile() {
		return fileList.size();
	}

	/**
	 * Makes a reservation of the space on the storage to store a file.
	 * 
	 * @param fileSize the size to be reserved in MB
	 * @return <tt>true</tt> if reservation succeeded, <tt>false</tt> otherwise
	 */
	@Override
	public boolean reserveSpace(int fileSize) {
		if (fileSize <= 0) {
			return false;
		}

		if (currentSize + fileSize >= capacity) {
			return false;
		}

		currentSize += fileSize;
		return true;
	}

	/**
	 * Adds a file for which the space has already been reserved. The time taken (in seconds) for
	 * adding the file can also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file to be added
	 * @return the time (in seconds) required to add the file
	 */
	@Override
	public double addReservedFile(File file) {
		if (file == null) {
			return 0;
		}

		currentSize -= file.getSize();
		double result = addFile(file);

		// if add file fails, then set the current size back to its old value
		if (result == 0.0) {
			currentSize += file.getSize();
		}

		return result;
	}

	/**
	 * Checks whether there is enough space on the storage for a certain file.
	 * 
	 * @param fileSize a FileAttribute object to compare to
	 * @return <tt>true</tt> if enough space available, <tt>false</tt> otherwise
	 */
	@Override
	public boolean hasPotentialAvailableSpace(int fileSize) {
		if (fileSize <= 0) {
			return false;
		}

		// check if enough space left
		if (getAvailableSpace() > fileSize) {
			return true;
		}

		Iterator<File> it = fileList.iterator();
		File file = null;
		int deletedFileSize = 0;

		// if not enough space, then if want to clear/delete some files
		// then check whether it still have space or not
		boolean result = false;
		while (it.hasNext()) {
			file = it.next();
			if (!file.isReadOnly()) {
				deletedFileSize += file.getSize();
			}

			if (deletedFileSize > fileSize) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets the total capacity of the storage in MB.
	 * 
	 * @return the capacity of the storage in MB
	 */
	@Override
	public double getCapacity() {
		return capacity;
	}

	/**
	 * Gets the current size of the stored files in MB.
	 * 
	 * @return the current size of the stored files in MB
	 */
	@Override
	public double getCurrentSize() {
		return currentSize;
	}

	/**
	 * Gets the name of the storage.
	 * 
	 * @return the name of this storage
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the latency of this harddrive in seconds.
	 * 
	 * @param latency the new latency in seconds
	 * @return <tt>true</tt> if the setting succeeded, <tt>false</tt> otherwise
	 */
	public boolean setLatency(double latency) {
		if (latency < 0) {
			return false;
		}

		this.latency = latency;
		return true;
	}

	/**
	 * Gets the latency of this harddrive in seconds.
	 * 
	 * @return the latency in seconds
	 */
	public double getLatency() {
		return latency;
	}

	/**
	 * Sets the maximum transfer rate of this storage system in MB/sec.
	 * 
	 * @param rate the maximum transfer rate in MB/sec
	 * @return <tt>true</tt> if the setting succeeded, <tt>false</tt> otherwise
	 */
	@Override
	public boolean setMaxTransferRate(int rate) {
		if (rate <= 0) {
			return false;
		}

		maxTransferRate = rate;
		return true;
	}

	/**
	 * Gets the maximum transfer rate of the storage in MB/sec.
	 * 
	 * @return the maximum transfer rate in MB/sec
	 */
	@Override
	public double getMaxTransferRate() {
		return maxTransferRate;
	}

	/**
	 * Sets the average seek time of the storage in seconds.
	 * 
	 * @param seekTime the average seek time in seconds
	 * @return <tt>true</tt> if the setting succeeded, <tt>false</tt> otherwise
	 */
	public boolean setAvgSeekTime(double seekTime) {
		return setAvgSeekTime(seekTime, null);
	}

	/**
	 * Sets the average seek time and a new generator of seek times in seconds. The generator
	 * determines a randomized seek time.
	 * 
	 * @param seekTime the average seek time in seconds
	 * @param gen the ContinuousGenerator which generates seek times
	 * @return <tt>true</tt> if the setting succeeded, <tt>false</tt> otherwise
	 */
	public boolean setAvgSeekTime(double seekTime, ContinuousDistribution gen) {
		if (seekTime <= 0.0) {
			return false;
		}

		avgSeekTime = seekTime;
		this.gen = gen;
		return true;
	}

	/**
	 * Gets the average seek time of the harddrive in seconds.
	 * 
	 * @return the average seek time in seconds
	 */
	public double getAvgSeekTime() {
		return avgSeekTime;
	}

	/**
	 * Gets the file with the specified name. The time taken (in seconds) for getting the file can
	 * also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the needed file
	 * @return the file with the specified filename
	 */
	@Override
	public File getFile(String fileName) {
		// check first whether file name is valid or not
		File obj = null;
		if (fileName == null || fileName.length() == 0) {
			Log.printLine(name + ".getFile(): Warning - invalid " + "file name.");
			return obj;
		}

		Iterator<File> it = fileList.iterator();
		int size = 0;
		int index = 0;
		boolean found = false;
		File tempFile = null;

		// find the file in the disk
		while (it.hasNext()) {
			tempFile = it.next();
			size += tempFile.getSize();
			if (tempFile.getName().equals(fileName)) {
				found = true;
				obj = tempFile;
				break;
			}

			index++;
		}

		// if the file is found, then determine the time taken to get it
		if (found) {
			obj = fileList.get(index);
			double seekTime = getSeekTime(size);
			double transferTime = getTransferTime(obj.getSize());

			// total time for this operation
			obj.setTransactionTime(seekTime + transferTime);
		}

		return obj;
	}

	/**
	 * Gets the list of file names located on this storage.
	 * 
	 * @return a List of file names
	 */
	@Override
	public List<String> getFileNameList() {
		return nameList;
	}

	/**
	 * Get the seek time for a file with the defined size. Given a file size in MB, this method
	 * returns a seek time for the file in seconds.
	 * 
	 * @param fileSize the size of a file in MB
	 * @return the seek time in seconds
	 */
	private double getSeekTime(int fileSize) {
		double result = 0;

		if (gen != null) {
			result += gen.sample();
		}

		if (fileSize > 0 && capacity != 0) {
			result += (fileSize / capacity);
		}

		return result;
	}

	/**
	 * Gets the transfer time of a given file.
	 * 
	 * @param fileSize the size of the transferred file
	 * @return the transfer time in seconds
	 */
	private double getTransferTime(int fileSize) {
		double result = 0;
		if (fileSize > 0 && capacity != 0) {
			result = (fileSize * maxTransferRate) / capacity;
		}

		return result;
	}

	/**
	 * Check if the file is valid or not. This method checks whether the given file or the file name
	 * of the file is valid. The method name parameter is used for debugging purposes, to output in
	 * which method an error has occured.
	 * 
	 * @param file the file to be checked for validity
	 * @param methodName the name of the method in which we check for validity of the file
	 * @return <tt>true</tt> if the file is valid, <tt>false</tt> otherwise
	 */
	private boolean isFileValid(File file, String methodName) {

		if (file == null) {
			Log.printLine(name + "." + methodName + ": Warning - the given file is null.");
			return false;
		}

		String fileName = file.getName();
		if (fileName == null || fileName.length() == 0) {
			Log.printLine(name + "." + methodName + ": Warning - invalid file name.");
			return false;
		}

		return true;
	}

	/**
	 * Adds a file to the storage. First, the method checks if there is enough space on the storage,
	 * then it checks if the file with the same name is already taken to avoid duplicate filenames. <br>
	 * The time taken (in seconds) for adding the file can also be found using
	 * {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file to be added
	 * @return the time taken (in seconds) for adding the specified file
	 */
	@Override
	public double addFile(File file) {
		double result = 0.0;
		// check if the file is valid or not
		if (!isFileValid(file, "addFile()")) {
			return result;
		}

		// check the capacity
		if (file.getSize() + currentSize > capacity) {
			Log.printLine(name + ".addFile(): Warning - not enough space" + " to store " + file.getName());
			return result;
		}

		// check if the same file name is alredy taken
		if (!contains(file.getName())) {
			double seekTime = getSeekTime(file.getSize());
			double transferTime = getTransferTime(file.getSize());

			fileList.add(file);               // add the file into the HD
			nameList.add(file.getName());     // add the name to the name list
			currentSize += file.getSize();    // increment the current HD size
			result = seekTime + transferTime;  // add total time
		}
		file.setTransactionTime(result);
		return result;
	}

	/**
	 * Adds a set of files to the storage. Runs through the list of files and save all of them. The
	 * time taken (in seconds) for adding each file can also be found using
	 * {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param list the files to be added
	 * @return the time taken (in seconds) for adding the specified files
	 */
	@Override
	public double addFile(List<File> list) {
		double result = 0.0;
		if (list == null || list.isEmpty()) {
			Log.printLine(name + ".addFile(): Warning - list is empty.");
			return result;
		}

		Iterator<File> it = list.iterator();
		File file = null;
		while (it.hasNext()) {
			file = it.next();
			result += addFile(file);    // add each file in the list
		}
		return result;
	}

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the file can also
	 * be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the file to be removed
	 * @return the deleted file
	 */
	@Override
	public File deleteFile(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return null;
		}

		Iterator<File> it = fileList.iterator();
		File file = null;
		while (it.hasNext()) {
			file = it.next();
			String name = file.getName();

			// if a file is found then delete
			if (fileName.equals(name)) {
				double result = deleteFile(file);
				file.setTransactionTime(result);
				break;
			} else {
				file = null;
			}
		}
		return file;
	}

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the file can also
	 * be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the file to be removed
	 * @param file the file which is removed from the storage is returned through this parameter
	 * @return the time taken (in seconds) for deleting the specified file
	 */
	@Override
	public double deleteFile(String fileName, File file) {
		return deleteFile(file);
	}

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the file can also
	 * be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file which is removed from the storage is returned through this parameter
	 * @return the time taken (in seconds) for deleting the specified file
	 */
	@Override
	public double deleteFile(File file) {
		double result = 0.0;
		// check if the file is valid or not
		if (!isFileValid(file, "deleteFile()")) {
			return result;
		}
		double seekTime = getSeekTime(file.getSize());
		double transferTime = getTransferTime(file.getSize());

		// check if the file is in the storage
		if (contains(file)) {
			fileList.remove(file);            // remove the file HD
			nameList.remove(file.getName());  // remove the name from name list
			currentSize -= file.getSize();    // decrement the current HD space
			result = seekTime + transferTime;  // total time
			file.setTransactionTime(result);
		}
		return result;
	}

	/**
	 * Checks whether a certain file is on the storage or not.
	 * 
	 * @param fileName the name of the file we are looking for
	 * @return <tt>true</tt> if the file is in the storage, <tt>false</tt> otherwise
	 */
	@Override
	public boolean contains(String fileName) {
		boolean result = false;
		if (fileName == null || fileName.length() == 0) {
			Log.printLine(name + ".contains(): Warning - invalid file name");
			return result;
		}
		// check each file in the list
		Iterator<String> it = nameList.iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (name.equals(fileName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks whether a certain file is on the storage or not.
	 * 
	 * @param file the file we are looking for
	 * @return <tt>true</tt> if the file is in the storage, <tt>false</tt> otherwise
	 */
	@Override
	public boolean contains(File file) {
		boolean result = false;
		if (!isFileValid(file, "contains()")) {
			return result;
		}

		result = contains(file.getName());
		return result;
	}

	/**
	 * Renames a file on the storage. The time taken (in seconds) for renaming the file can also be
	 * found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file we would like to rename
	 * @param newName the new name of the file
	 * @return <tt>true</tt> if the renaming succeeded, <tt>false</tt> otherwise
	 */
	@Override
	public boolean renameFile(File file, String newName) {
		// check whether the new filename is conflicting with existing ones
		// or not
		boolean result = false;
		if (contains(newName)) {
			return result;
		}

		// replace the file name in the file (physical) list
		File obj = getFile(file.getName());
		if (obj == null) {
			return result;
		} else {
			obj.setName(newName);
		}

		// replace the file name in the name list
		Iterator<String> it = nameList.iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (name.equals(file.getName())) {
				file.setTransactionTime(0);
				nameList.remove(name);
				nameList.add(newName);
				result = true;
				break;
			}
		}

		return result;
	}

}
