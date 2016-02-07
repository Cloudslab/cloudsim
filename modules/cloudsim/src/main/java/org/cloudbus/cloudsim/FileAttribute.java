/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.Date;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Stores related information regarding to a {@link gridsim.datagrid.File} entity.
 * 
 * @author Uros Cibej
 * @author Anthony Sulistio
 * @since CloudSim Toolkit 1.0
 * 
 * @todo Some attributes of this class may be duplicated from the {@link File} class,
 * such as name (logical file name), that is clearly related to the file.
 * There would be a relation between File and FileAttribute. There is a lot of duplicated
 * methods to, such as {@link #setMasterCopy(boolean)} or {@link #isReadOnly()}
 */
public class FileAttribute {

        /** Logical file name. */
	private String name;           
        /** Owner name of this file. */
	private String ownerName;      
        /** File ID given by a Replica Catalogue. */
	private int id;                
        /** File type, for instance raw, reconstructed, etc. */
	private int type;              
        /** File size in byte. */
	private int size;              
        /** Check sum. */
	private int checksum;          
        /** Last updated time (sec) - relative. */
	private double lastUpdateTime; 
        /** Creation time (ms) - abosulte/relative. */
	private long creationTime;     
        /** Price of the file. */
	private double cost;           
        /** Indicates if the file is a master copy or not. 
         * If the attribute is false, it means the file is a replica. */
	private boolean masterCopy;    
        /** Indicates if the file is read-only or not. */
	private boolean readOnly;      
        /** Resource ID storing this file. */
	private int resourceId;        

	/**
	 * Creates a new FileAttribute object.
	 * 
	 * @param fileName file name
	 * @param fileSize size of this file (in bytes)
	 * @throws ParameterException This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>the file name is empty or <tt>null</tt>
	 *             <li>the file size is zero or negative numbers
	 *             </ul>
	 */
	public FileAttribute(String fileName, int fileSize) throws ParameterException {
		// check for errors in the input
		if (fileName == null || fileName.length() == 0) {
			throw new ParameterException("FileAttribute(): Error - invalid file name.");
		}

		if (fileSize <= 0) {
			throw new ParameterException("FileAttribute(): Error - size <= 0.");
		}

		size = fileSize;
		name = fileName;

		// set the file creation time. This is absolute time
		Date date = CloudSim.getSimulationCalendar().getTime();
		if (date == null) {
			creationTime = 0;
		} else {
			creationTime = date.getTime();
		}

		ownerName = null;
		id = File.NOT_REGISTERED;
		checksum = 0;
		type = File.TYPE_UNKOWN;
		lastUpdateTime = 0;
		cost = 0;
		resourceId = -1;
		masterCopy = true;
		readOnly = false;
	}

	/**
	 * Copy the values of the object into a given FileAttribute instance.
	 * 
	 * @param attr the destination FileAttribute object to copy the current object to
	 * @return <tt>true</tt> if the copy operation is successful, <tt>false</tt> otherwise
	 */
	public boolean copyValue(FileAttribute attr) {
		if (attr == null) {
			return false;
		}

		attr.setFileSize(size);
		attr.setResourceID(resourceId);
		attr.setOwnerName(ownerName);
		attr.setUpdateTime(lastUpdateTime);
		attr.setRegistrationId(id);
		attr.setType(type);
		attr.setChecksum(checksum);
		attr.setCost(cost);
		attr.setMasterCopy(masterCopy);
		attr.setReadOnly(readOnly);
		attr.setName(name);
		attr.setCreationTime(creationTime);

		return true;
	}

	/**
	 * Sets the file creation time (in millisecond).
	 * 
	 * @param creationTime the file creation time (in millisecond)
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setCreationTime(long creationTime) {
		if (creationTime <= 0) {
			return false;
		}

		this.creationTime = creationTime;
		return true;
	}

	/**
	 * Gets the file creation time (in millisecond).
	 * 
	 * @return the file creation time (in millisecond)
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Sets the resource ID that stores the file.
	 * 
	 * @param resourceID a resource ID
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setResourceID(int resourceID) {
		if (resourceID == -1) {
			return false;
		}

		resourceId = resourceID;
		return true;
	}

	/**
	 * Gets the resource ID that stores the file.
	 * 
	 * @return the resource ID
	 */
	public int getResourceID() {
		return resourceId;
	}

	/**
	 * Sets the owner name of the file.
	 * 
	 * @param name the owner name
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setOwnerName(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}

		ownerName = name;
		return true;
	}

	/**
	 * Gets the owner name of the file.
	 * 
	 * @return the owner name or <tt>null</tt> if empty
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * Gets the size of the object (in byte). <br/>
	 * NOTE: This object size is NOT the actual file size. Moreover, this size is used for
	 * transferring this object over a network.
	 * 
	 * @return the object size (in byte)
	 */
	public int getAttributeSize() {
		int length = DataCloudTags.PKT_SIZE;
		if (ownerName != null) {
			length += ownerName.length();
		}

		if (name != null) {
			length += name.length();
		}

		return length;
	}

	/**
	 * Sets the file size (in MBytes).
	 * 
	 * @param fileSize the file size (in MBytes)
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFileSize(int fileSize) {
		if (fileSize < 0) {
			return false;
		}

		size = fileSize;
		return true;
	}

	/**
	 * Gets the file size (in MBytes).
	 * 
	 * @return the file size (in MBytes)
	 */
	public int getFileSize() {
		return size;
	}

	/**
	 * Gets the file size (in bytes).
	 * 
	 * @return the file size (in bytes)
	 */
	public int getFileSizeInByte() {
		return size * Consts.MILLION;   // 1e6
		// return size * 1048576; // 1e6 - more accurate
	}

	/**
	 * Sets the last update time of the file (in seconds). <br/>
	 * NOTE: This time is relative to the start time. Preferably use
	 * {@link gridsim.CloudSim#clock()} method.
	 * 
	 * @param time the last update time (in seconds)
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setUpdateTime(double time) {
		if (time <= 0 || time < lastUpdateTime) {
			return false;
		}

		lastUpdateTime = time;
		return true;
	}

	/**
	 * Gets the last update time (in seconds).
	 * 
	 * @return the last update time (in seconds)
	 */
	public double getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * Sets the file registration ID (published by a Replica Catalogue entity).
	 * 
	 * @param id registration ID
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setRegistrationId(int id) {
		if (id < 0) {
			return false;
		}

		this.id = id;
		return true;
	}

	/**
	 * Gets the file registration ID.
	 * 
	 * @return registration ID
	 */
	public int getRegistrationID() {
		return id;
	}

	/**
	 * Sets the file type (for instance raw, tag, etc).
	 * 
	 * @param type a file type
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setType(int type) {
		if (type < 0) {
			return false;
		}

		this.type = type;
		return true;
	}

	/**
	 * Gets the file type.
	 * 
	 * @return file type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the checksum of the file.
	 * 
	 * @param checksum the checksum of this file
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setChecksum(int checksum) {
		if (checksum < 0) {
			return false;
		}

		this.checksum = checksum;
		return true;
	}

	/**
	 * Gets the file checksum.
	 * 
	 * @return file checksum
	 */
	public int getChecksum() {
		return checksum;
	}

	/**
	 * Sets the cost associated with the file.
	 * 
	 * @param cost cost of this file
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setCost(double cost) {
		if (cost < 0) {
			return false;
		}

		this.cost = cost;
		return true;
	}

	/**
	 * Gets the cost associated with the file.
	 * 
	 * @return the cost of this file
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Checks if the file is already registered to a Replica Catalogue.
	 * 
	 * @return <tt>true</tt> if it is registered, <tt>false</tt> otherwise
	 */
	public boolean isRegistered() {
		boolean result = true;
		if (id == File.NOT_REGISTERED) {
			result = false;
		}

		return result;
	}

	/**
	 * Marks the file as a master copy or replica.
	 * 
	 * @param masterCopy a flag denotes <tt>true</tt> for master copy or <tt>false</tt> for a
	 *            replica
	 */
	public void setMasterCopy(boolean masterCopy) {
		this.masterCopy = masterCopy;
	}

	/**
	 * Checks whether the file is a master copy or replica.
	 * 
	 * @return <tt>true</tt> if it is a master copy or <tt>false</tt> otherwise
	 */
	public boolean isMasterCopy() {
		return masterCopy;
	}

	/**
	 * Marks this file as a read only or not.
	 * 
	 * @param readOnly a flag denotes <tt>true</tt> for read only or <tt>false</tt> for re-writeable
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Checks whether this file is a read only or not.
	 * 
	 * @return <tt>true</tt> if it is a read only or <tt>false</tt> otherwise
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param name the file name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the file name.
	 * 
	 * @return the file name
	 */
	public String getName() {
		return name;
	}

}
