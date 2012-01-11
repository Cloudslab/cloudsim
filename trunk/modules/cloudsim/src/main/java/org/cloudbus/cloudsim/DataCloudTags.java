/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * This class contains additional tags for the DataCloud functionalities, such as file information
 * retrieval, file transfers, and storage info.
 * 
 * @author Uros Cibej
 * @author Anthony Sulistio
 * @since CloudSim Toolkit 1.0
 */
public final class DataCloudTags {

	// to prevent a conflict with the existing CloudSimTags values
	private static final int BASE = 400;        // for other general tags

	private static final int RM_BASE = 500;     // for Replica Manager tags

	private static final int CTLG_BASE = 600;   // for catalogue tags

	// ////////// GENERAL TAGS

	/** Default Maximum Transmission Unit (MTU) of a link in bytes */
	public static final int DEFAULT_MTU = 1500;

	/** The default packet size (in byte) for sending events to other entity. */
	public static final int PKT_SIZE = DEFAULT_MTU * 100;  // in bytes

	/** The default storage size (10 GByte) */
	public static final int DEFAULT_STORAGE_SIZE = 10000000; // 10 GB in bytes

	/** Registers a Replica Catalogue (RC) entity to a Data GIS */
	public static final int REGISTER_REPLICA_CTLG = BASE + 1;

	/**
	 * Denotes a list of all Replica Catalogue (RC) entities that are listed in this regional Data
	 * GIS entity. This tag should be called from a user to Data GIS.
	 */
	public static final int INQUIRY_LOCAL_RC_LIST = BASE + 2;

	/**
	 * Denotes a list of Replica Catalogue (RC) entities that are listed in other regional Data GIS
	 * entities. This tag should be called from a user to Data GIS.
	 */
	public static final int INQUIRY_GLOBAL_RC_LIST = BASE + 3;

	/**
	 * Denotes a list of Replica Catalogue IDs. This tag should be called from a Regional Data GIS
	 * to another
	 */
	public static final int INQUIRY_RC_LIST = BASE + 4;

	/**
	 * Denotes a result regarding to a list of Replica Catalogue IDs. This tag should be called from
	 * a Regional Data GIS to a sender Regional Data GIS.
	 */
	public static final int INQUIRY_RC_RESULT = BASE + 5;

	/**
	 * Denotes the submission of a DataCloudlet. This tag is normally used between user and
	 * DataCloudResource entity.
	 */
	public static final int DATAcloudlet_SUBMIT = BASE + 6;

	// ////////// REPLICA MANAGER TAGS

	// ***********************User <--> RM******************************//

	/**
	 * Requests for a file that is stored on the local storage(s).<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_DELIVERY}.
	 */
	public static final int FILE_REQUEST = RM_BASE + 1;

	/**
	 * Sends the file to the requester. The format of the reply is File or null if error happens
	 */
	public static final int FILE_DELIVERY = RM_BASE + 2;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a master file to be added to the local storage(s).<br>
	 * The format of this request is Object[2] = {File obj, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_ADD_MASTER_RESULT}.
	 */
	public static final int FILE_ADD_MASTER = RM_BASE + 10;

	/**
	 * Sends the result of adding a master file back to sender.<br>
	 * The format of the reply is Object[3] = {String lfn, Integer uniqueID, Integer resultID}.<br>
	 * NOTE: The result id is in the form of FILE_ADD_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int FILE_ADD_MASTER_RESULT = RM_BASE + 11;

	/**
	 * Requests for a replica file to be added from the local storage(s).<br>
	 * The format of this request is Object[2] = {File obj, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_ADD_REPLICA_RESULT}.
	 */
	public static final int FILE_ADD_REPLICA = RM_BASE + 12;

	/**
	 * Sends the result of adding a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of FILE_ADD_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int FILE_ADD_REPLICA_RESULT = RM_BASE + 13;

	/** Denotes that file addition is successful */
	public static final int FILE_ADD_SUCCESSFUL = RM_BASE + 20;

	/** Denotes that file addition is failed because the storage is full */
	public static final int FILE_ADD_ERROR_STORAGE_FULL = RM_BASE + 21;

	/** Denotes that file addition is failed because the given file is empty */
	public static final int FILE_ADD_ERROR_EMPTY = RM_BASE + 22;

	/**
	 * Denotes that file addition is failed because the file already exists in the catalogue and it
	 * is read-only file
	 */
	public static final int FILE_ADD_ERROR_EXIST_READ_ONLY = RM_BASE + 23;

	/** Denotes that file addition is failed due to an unknown error */
	public static final int FILE_ADD_ERROR = RM_BASE + 24;

	/**
	 * Denotes that file addition is failed because access/permission denied or not authorized
	 */
	public static final int FILE_ADD_ERROR_ACCESS_DENIED = RM_BASE + 25;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a master file to be deleted from the local storage(s).<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_DELETE_MASTER_RESULT}.
	 */
	public static final int FILE_DELETE_MASTER = RM_BASE + 30;

	/**
	 * Sends the result of deleting a master file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of FILE_DELETE_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int FILE_DELETE_MASTER_RESULT = RM_BASE + 31;

	/**
	 * Requests for a replica file to be deleted from the local storage(s).<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_DELETE_REPLICA_RESULT}.
	 */
	public static final int FILE_DELETE_REPLICA = RM_BASE + 32;

	/**
	 * Sends the result of deleting a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of FILE_DELETE_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int FILE_DELETE_REPLICA_RESULT = RM_BASE + 33;

	/** Denotes that file deletion is successful */
	public static final int FILE_DELETE_SUCCESSFUL = RM_BASE + 40;

	/** Denotes that file deletion is failed due to an unknown error */
	public static final int FILE_DELETE_ERROR = RM_BASE + 41;

	/** Denotes that file deletion is failed because it is a read-only file */
	public static final int FILE_DELETE_ERROR_READ_ONLY = RM_BASE + 42;

	/**
	 * Denotes that file deletion is failed because the file does not exist in the storage nor
	 * catalogue
	 */
	public static final int FILE_DELETE_ERROR_DOESNT_EXIST = RM_BASE + 43;

	/**
	 * Denotes that file deletion is failed because it is currently used by others
	 */
	public static final int FILE_DELETE_ERROR_IN_USE = RM_BASE + 44;

	/**
	 * Denotes that file deletion is failed because access/permission denied or not authorized
	 */
	public static final int FILE_DELETE_ERROR_ACCESS_DENIED = RM_BASE + 45;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a file to be modified from the local storage(s).<br>
	 * The format of this request is Object[2] = {File obj, Integer senderID}.<br>
	 * The reply tag name is {@link #FILE_MODIFY_RESULT}.
	 */
	public static final int FILE_MODIFY = RM_BASE + 50;

	/**
	 * Sends the result of deleting a file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of FILE_MODIFY_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int FILE_MODIFY_RESULT = RM_BASE + 51;

	/** Denotes that file modification is successful */
	public static final int FILE_MODIFY_SUCCESSFUL = RM_BASE + 60;

	/** Denotes that file modification is failed due to an unknown error */
	public static final int FILE_MODIFY_ERROR = RM_BASE + 61;

	/**
	 * Denotes that file modification is failed because it is a read-only file
	 */
	public static final int FILE_MODIFY_ERROR_READ_ONLY = RM_BASE + 62;

	/**
	 * Denotes that file modification is failed because the file does not exist
	 */
	public static final int FILE_MODIFY_ERROR_DOESNT_EXIST = RM_BASE + 63;

	/**
	 * Denotes that file modification is failed because the file is currently used by others
	 */
	public static final int FILE_MODIFY_ERROR_IN_USE = RM_BASE + 64;

	/**
	 * Denotes that file modification is failed because access/permission denied or not authorized
	 */
	public static final int FILE_MODIFY_ERROR_ACCESS_DENIED = RM_BASE + 65;

	// ////////// REPLICA CATALOGUE TAGS

	// ***********************User<-->RC******************************//

	/**
	 * Denotes the request for a location of a replica file.<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #CTLG_REPLICA_DELIVERY}.<br>
	 * NOTE: This request only ask for one location only not all.
	 */
	public static final int CTLG_GET_REPLICA = CTLG_BASE + 1;

	/**
	 * Sends the result for a location of a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resourceID}.<br>
	 * NOTE: The resourceID could be <tt>-1</tt> if not found.
	 */
	public static final int CTLG_REPLICA_DELIVERY = CTLG_BASE + 2;

	/**
	 * Denotes the request for all locations of a replica file.<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #CTLG_REPLICA_LIST_DELIVERY}.
	 */
	public static final int CTLG_GET_REPLICA_LIST = CTLG_BASE + 3;

	/**
	 * Sends the result for all locations of a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, List locationList}.<br>
	 * NOTE: The locationList could be <tt>null</tt> if not found.
	 */
	public static final int CTLG_REPLICA_LIST_DELIVERY = CTLG_BASE + 4;

	/**
	 * Denotes the request to get the attribute of a file.<br>
	 * The format of this request is Object[2] = {String lfn, Integer senderID}.<br>
	 * The reply tag name is {@link #CTLG_FILE_ATTR_DELIVERY}.
	 */
	public static final int CTLG_GET_FILE_ATTR = CTLG_BASE + 5;

	/**
	 * Sends the result for a file attribute back to sender.<br>
	 * The format of the reply is {FileAttribute fileAttr}.<br>
	 * NOTE: The fileAttr could be <tt>null</tt> if not found.
	 */
	public static final int CTLG_FILE_ATTR_DELIVERY = CTLG_BASE + 6;

	/**
	 * Denotes the request to get a list of file attributes based on the given filter.<br>
	 * The format of this request is Object[2] = {Filter filter, Integer senderID}.<br>
	 * The reply tag name is {@link #CTLG_FILTER_DELIVERY}.
	 */
	public static final int CTLG_FILTER = CTLG_BASE + 7;

	/**
	 * Sends the result for a list of file attributes back to sender.<br>
	 * The format of the reply is {List attrList}.<br>
	 * NOTE: The attrList could be <tt>null</tt> if not found.
	 */
	public static final int CTLG_FILTER_DELIVERY = CTLG_BASE + 8;

	// ***********************RM<-->RC******************************//

	/**
	 * Denotes the request to register / add a master file to the Replica Catalogue.<br>
	 * The format of this request is Object[3] = {String filename, FileAttribute attr, Integer
	 * resID}.<br>
	 * The reply tag name is {@link #CTLG_ADD_MASTER_RESULT}.
	 */
	public static final int CTLG_ADD_MASTER = CTLG_BASE + 10;

	/**
	 * Sends the result of registering a master file back to sender.<br>
	 * The format of the reply is Object[3] = {String filename, Integer uniqueID, Integer resultID}.<br>
	 * NOTE: The result id is in the form of CTLG_ADD_MASTER_XXXX where XXXX means the error/success
	 * message
	 */
	public static final int CTLG_ADD_MASTER_RESULT = CTLG_BASE + 11;

	/** Denotes that master file addition is successful */
	public static final int CTLG_ADD_MASTER_SUCCESSFUL = CTLG_BASE + 12;

	/** Denotes that master file addition is failed due to an unknown error */
	public static final int CTLG_ADD_MASTER_ERROR = CTLG_BASE + 13;

	/**
	 * Denotes that master file addition is failed due to the catalogue is full
	 */
	public static final int CTLG_ADD_MASTER_ERROR_FULL = CTLG_BASE + 14;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to de-register / delete a master file from the Replica Catalogue.<br>
	 * The format of this request is Object[2] = {String lfn, Integer resourceID}.<br>
	 * The reply tag name is {@link #CTLG_DELETE_MASTER_RESULT}.
	 */
	public static final int CTLG_DELETE_MASTER = CTLG_BASE + 20;

	/**
	 * Sends the result of de-registering a master file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of CTLG_DELETE_MASTER_XXXX where XXXX means the
	 * error/success message
	 */
	public static final int CTLG_DELETE_MASTER_RESULT = CTLG_BASE + 21;

	/** Denotes that master file deletion is successful */
	public static final int CTLG_DELETE_MASTER_SUCCESSFUL = CTLG_BASE + 22;

	/** Denotes that master file deletion is failed due to an unknown error */
	public static final int CTLG_DELETE_MASTER_ERROR = CTLG_BASE + 23;

	/**
	 * Denotes that master file deletion is failed because the file does not exist in the catalogue
	 */
	public static final int CTLG_DELETE_MASTER_DOESNT_EXIST = CTLG_BASE + 24;

	/**
	 * Denotes that master file deletion is failed because replica files are still in the catalogue.
	 * All replicas need to be deleted first.
	 */
	public static final int CTLG_DELETE_MASTER_REPLICAS_EXIST = CTLG_BASE + 25;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to register / add a replica file to the Replica Catalogue.<br>
	 * The format of this request is Object[2] = {String lfn, Integer resourceID}.<br>
	 * The reply tag name is {@link #CTLG_ADD_REPLICA_RESULT}.
	 */
	public static final int CTLG_ADD_REPLICA = CTLG_BASE + 30;

	/**
	 * Sends the result of registering a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of CTLG_ADD_REPLICA_XXXX where XXXX means the
	 * error/success message
	 */
	public static final int CTLG_ADD_REPLICA_RESULT = CTLG_BASE + 31;

	/** Denotes that replica file addition is successful */
	public static final int CTLG_ADD_REPLICA_SUCCESSFUL = CTLG_BASE + 32;

	/** Denotes that replica file addition is failed due to an unknown error */
	public static final int CTLG_ADD_REPLICA_ERROR = CTLG_BASE + 33;

	/**
	 * Denotes that replica file addition is failed because the given file name does not exist in
	 * the catalogue
	 */
	public static final int CTLG_ADD_REPLICA_ERROR_DOESNT_EXIST = CTLG_BASE + 34;

	/**
	 * Denotes that replica file addition is failed due to the catalogue is full
	 */
	public static final int CTLG_ADD_REPLICA_ERROR_FULL = CTLG_BASE + 35;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to de-register / delete a replica file from the Replica Catalogue.<br>
	 * The format of this request is Object[2] = {String lfn, Integer resourceID}.<br>
	 * The reply tag name is {@link #CTLG_DELETE_REPLICA_RESULT}.
	 */
	public static final int CTLG_DELETE_REPLICA = CTLG_BASE + 40;

	/**
	 * Sends the result of de-registering a replica file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of CTLG_DELETE_REPLICA_XXXX where XXXX means the
	 * error/success message
	 */
	public static final int CTLG_DELETE_REPLICA_RESULT = CTLG_BASE + 41;

	/** Denotes that replica file deletion is successful */
	public static final int CTLG_DELETE_REPLICA_SUCCESSFUL = CTLG_BASE + 42;

	/** Denotes that replica file deletion is failed due to an unknown error */
	public static final int CTLG_DELETE_REPLICA_ERROR = CTLG_BASE + 43;

	/**
	 * Denotes that replica file deletion is failed because the file does not exist in the catalogue
	 */
	public static final int CTLG_DELETE_REPLICA_ERROR_DOESNT_EXIST = CTLG_BASE + 44;

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to modify an existing master file information stored in the Replica
	 * Catalogue.<br>
	 * The format of this request is Object[3] = {String filename, FileAttribute attr, Integer
	 * resID}.<br>
	 * The reply tag name is {@link #CTLG_MODIFY_MASTER_RESULT}.
	 */
	public static final int CTLG_MODIFY_MASTER = CTLG_BASE + 50;

	/**
	 * Sends the result of modifying a master file back to sender.<br>
	 * The format of the reply is Object[2] = {String lfn, Integer resultID}.<br>
	 * NOTE: The result id is in the form of CTLG_MODIFY_MASTER_XXXX where XXXX means the
	 * error/success message
	 */
	public static final int CTLG_MODIFY_MASTER_RESULT = CTLG_BASE + 51;

	/** Denotes that master file deletion is successful */
	public static final int CTLG_MODIFY_MASTER_SUCCESSFUL = CTLG_BASE + 52;

	/**
	 * Denotes that master file modification is failed due to an unknown error
	 */
	public static final int CTLG_MODIFY_MASTER_ERROR = CTLG_BASE + 53;

	/**
	 * Denotes that master file modification is failed because the file does not exist in the
	 * catalogue
	 */
	public static final int CTLG_MODIFY_MASTER_ERROR_DOESNT_EXIST = CTLG_BASE + 54;

	/**
	 * Denotes that master file modification is failed because the file attribute is set to a
	 * read-only
	 */
	public static final int CTLG_MODIFY_MASTER_ERROR_READ_ONLY = CTLG_BASE + 55;

	// /////////////////////////////////////////////////////////////////////

	/** Private Constructor */
	private DataCloudTags() {
		throw new UnsupportedOperationException("DataCloudTags cannot be instantiated");
	}

}
