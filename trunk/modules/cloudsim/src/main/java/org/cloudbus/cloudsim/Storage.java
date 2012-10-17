/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

/**
 * An interface which defines the desired functionality of a storage system in a Data Cloud. The
 * classes that implement this interface should simulate the characteristics of different storage
 * systems by setting the capacity of the storage and the maximum transfer rate. The transfer rate
 * defines the time required to execute some common operations on the storage, e.g. storing a file,
 * getting a file and deleting a file.
 * 
 * @author Uros Cibej
 * @author Anthony Sulistio
 * @since CloudSim Toolkit 1.0
 */
public interface Storage {

	/**
	 * Gets the name of the storage.
	 * 
	 * @return the name of this storage
	 */
	String getName();

	/**
	 * Gets the total capacity of the storage in MByte.
	 * 
	 * @return the capacity of the storage in MB
	 */
	double getCapacity();

	/**
	 * Gets the current size of the storage in MByte.
	 * 
	 * @return the current size of the storage in MB
	 */
	double getCurrentSize();

	/**
	 * Gets the maximum transfer rate of the storage in MByte/sec.
	 * 
	 * @return the maximum transfer rate in MB/sec
	 */
	double getMaxTransferRate();

	/**
	 * Gets the available space on this storage in MByte.
	 * 
	 * @return the available space in MB
	 */
	double getAvailableSpace();

	/**
	 * Sets the maximum transfer rate of this storage system in MByte/sec.
	 * 
	 * @param rate the maximum transfer rate in MB/sec
	 * @return <tt>true</tt> if the setting succeeded, <tt>false</tt> otherwise
	 */
	boolean setMaxTransferRate(int rate);

	/**
	 * Checks if the storage is full or not.
	 * 
	 * @return <tt>true</tt> if the storage is full, <tt>false</tt> otherwise
	 */
	boolean isFull();

	/**
	 * Gets the number of files stored on this storage.
	 * 
	 * @return the number of stored files
	 */
	int getNumStoredFile();

	/**
	 * Makes a reservation of the space on the storage to store a file.
	 * 
	 * @param fileSize the size to be reserved in MB
	 * @return <tt>true</tt> if reservation succeeded, <tt>false</tt> otherwise
	 */
	boolean reserveSpace(int fileSize);

	/**
	 * Adds a file for which the space has already been reserved. The time taken (in seconds) for
	 * adding the specified file can also be found using
	 * {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file to be added
	 * @return the time (in seconds) required to add the file
	 */
	double addReservedFile(File file);

	/**
	 * Checks whether there is enough space on the storage for a certain file.
	 * 
	 * @param fileSize a FileAttribute object to compare to
	 * @return <tt>true</tt> if enough space available, <tt>false</tt> otherwise
	 */
	boolean hasPotentialAvailableSpace(int fileSize);

	/**
	 * Gets the file with the specified name. The time taken (in seconds) for getting the specified
	 * file can also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the needed file
	 * @return the file with the specified filename
	 */
	File getFile(String fileName);

	/**
	 * Gets the list of file names located on this storage.
	 * 
	 * @return a List of file names
	 */
	List<String> getFileNameList();

	/**
	 * Adds a file to the storage. The time taken (in seconds) for adding the specified file can
	 * also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file to be added
	 * @return the time taken (in seconds) for adding the specified file
	 */
	double addFile(File file);

	/**
	 * Adds a set of files to the storage. The time taken (in seconds) for adding each file can also
	 * be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param list the files to be added
	 * @return the time taken (in seconds) for adding the specified files
	 */
	double addFile(List<File> list);

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the specified file
	 * can be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the file to be removed
	 * @return the deleted file.
	 */
	File deleteFile(String fileName);

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the specified file
	 * can also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param fileName the name of the file to be removed
	 * @param file the file which is removed from the storage is returned through this parameter
	 * @return the time taken (in seconds) for deleting the specified file
	 */
	double deleteFile(String fileName, File file);

	/**
	 * Removes a file from the storage. The time taken (in seconds) for deleting the specified file
	 * can also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file which is removed from the storage is returned through this parameter
	 * @return the time taken (in seconds) for deleting the specified file
	 */
	double deleteFile(File file);

	/**
	 * Checks whether a file is stored in the storage or not.
	 * 
	 * @param fileName the name of the file we are looking for
	 * @return <tt>true</tt> if the file is in the storage, <tt>false</tt> otherwise
	 */
	boolean contains(String fileName);

	/**
	 * Checks whether a file is stored in the storage or not.
	 * 
	 * @param file the file we are looking for
	 * @return <tt>true</tt> if the file is in the storage, <tt>false</tt> otherwise
	 */
	boolean contains(File file);

	/**
	 * Renames a file on the storage. The time taken (in seconds) for renaming the specified file
	 * can also be found using {@link gridsim.datagrid.File#getTransactionTime()}.
	 * 
	 * @param file the file we would like to rename
	 * @param newName the new name of the file
	 * @return <tt>true</tt> if the renaming succeeded, <tt>false</tt> otherwise
	 */
	boolean renameFile(File file, String newName);

}
