/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.Iterator;
import java.util.List;

/**
 * SANStorage represents a storage area network composed of a set of harddisks connected in a LAN.
 * Capacity of individual disks are abstracted, thus only the overall capacity of the SAN is
 * considered. WARNING: This class is not yet fully functional. Effects of network contention are
 * not considered in the simulation. So, time for file transfer is underestimated in the presence of
 * high network load.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0
 */
public class SanStorage extends HarddriveStorage {

	/** The bandwidth. */
	double bandwidth;

	/** The network latency. */
	double networkLatency;

	/**
	 * Creates a new SAN with a given capacity, latency, and bandwidth of the network connection.
	 * 
	 * @param capacity Storage device capacity
	 * @param bandwidth Network bandwidth
	 * @param networkLatency Network latency
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public SanStorage(double capacity, double bandwidth, double networkLatency) throws ParameterException {
		super(capacity);
		this.bandwidth = bandwidth;
		this.networkLatency = networkLatency;
	}

	/**
	 * Creates a new SAN with a given capacity, latency, and bandwidth of the network connection.
	 * 
	 * @param name the name of the new harddrive storage
	 * @param capacity Storage device capacity
	 * @param bandwidth Network bandwidth
	 * @param networkLatency Network latency
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public SanStorage(String name, double capacity, double bandwidth, double networkLatency)
			throws ParameterException {
		super(name, capacity);
		this.bandwidth = bandwidth;
		this.networkLatency = networkLatency;
	}

	/**
	 * Adds a file for which the space has already been reserved.
	 * 
	 * @param file the file to be added
	 * @return the time (in seconds) required to add the file
	 */
	@Override
	public double addReservedFile(File file) {
		double time = super.addReservedFile(file);
		time += networkLatency;
		time += file.getSize() * bandwidth;

		return time;
	}

	/**
	 * Gets the maximum transfer rate of the storage in MB/sec.
	 * 
	 * @return the maximum transfer rate in MB/sec
	 */
	@Override
	public double getMaxTransferRate() {

		double diskRate = super.getMaxTransferRate();

		// the max transfer rate is the minimum between
		// the network bandwidth and the disk rate
		if (diskRate < bandwidth) {
			return diskRate;
		}
		return bandwidth;
	}

	/**
	 * Adds a file to the storage.
	 * 
	 * @param file the file to be added
	 * @return the time taken (in seconds) for adding the specified file
	 */
	@Override
	public double addFile(File file) {
		double time = super.addFile(file);

		time += networkLatency;
		time += file.getSize() * bandwidth;

		return time;
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
		if (list == null || list.size() == 0) {
			Log.printLine(getName() + ".addFile(): Warning - list is empty.");
			return result;
		}

		Iterator<File> it = list.iterator();
		File file = null;
		while (it.hasNext()) {
			file = it.next();
			result += this.addFile(file);    // add each file in the list
		}
		return result;
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
		return this.deleteFile(file);
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
		double time = super.deleteFile(file);

		time += networkLatency;
		time += file.getSize() * bandwidth;

		return time;
	}

}
