/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * This class contains additional tags for the DataCloud functionalities, such as file information
 * retrieval, file transfers, and storage info.
 * 
 * @author Uros Cibej
 * @author Anthony Sulistio
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public enum DataCloudTags implements CloudSimTags {
	// ////////// GENERAL TAGS

	/** Registers a Replica Catalogue (RC) entity to a Data GIS. */
	REGISTER_REPLICA_CTLG,

	/**
	 * Denotes a list of all Replica Catalogue (RC) entities that are listed in this regional Data
	 * GIS entity. This tag should be called from a user to Data GIS.
	 */
	INQUIRY_LOCAL_RC_LIST,

	/**
	 * Denotes a list of Replica Catalogue (RC) entities that are listed in other regional Data GIS
	 * entities. This tag should be called from a user to Data GIS.
	 */
	INQUIRY_GLOBAL_RC_LIST,

	/**
	 * Denotes a list of Replica Catalogue IDs. This tag should be called from a Regional Data GIS
	 * to another
	 */
	INQUIRY_RC_LIST,

	/**
	 * Denotes a result regarding to a list of Replica Catalogue IDs. This tag should be called from
	 * a Regional Data GIS to a sender Regional Data GIS.
	 */
	INQUIRY_RC_RESULT,

	/**
	 * Denotes the submission of a DataCloudlet. This tag is normally used between user and
	 * DataCloudResource entity.
	 */
	DATAcloudlet_SUBMIT,

	// ////////// REPLICA MANAGER TAGS

	// ***********************User <--> RM******************************//

	/**
	 * Requests for a file that is stored on the local storage(s).
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.<br/>
	 * The reply tag name is {@link #FILE_DELIVERY}.
	 */
	FILE_REQUEST,

	/**
	 * Sends the file to the requester. The format of the reply is File or null if error happens
	 */
	FILE_DELIVERY,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a master file to be added to the local storage(s).
	 * <br/>The format of this request is Object[2] = {File obj, Integer senderID}.<br/>
	 * The reply tag name is {@link #FILE_ADD_MASTER_RESULT}.
	 */
	FILE_ADD_MASTER,

	/**
	 * Sends the result of adding a master file back to sender.
	 * <br/>The format of the reply is Object[3] = {String lfn, Integer uniqueID, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of FILE_ADD_XXXX where XXXX means the error/success
	 * message.
	 */
	FILE_ADD_MASTER_RESULT,

	/**
	 * Requests for a replica file to be added from the local storage(s).
	 * <br/>The format of this request is Object[2] = {File obj, Integer senderID}.
	 * <br/>The reply tag name is {@link #FILE_ADD_REPLICA_RESULT}.
	 */
	FILE_ADD_REPLICA,

	/**
	 * Sends the result of adding a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of FILE_ADD_XXXX where XXXX means the error/success
	 * message
	 */
	FILE_ADD_REPLICA_RESULT,

	/** Denotes that file addition is successful. */
	FILE_ADD_SUCCESSFUL,

	/** Denotes that file addition is failed because the storage is full. */
	FILE_ADD_ERROR_STORAGE_FULL,

	/** Denotes that file addition is failed because the given file is empty. */
	FILE_ADD_ERROR_EMPTY,

	/**
	 * Denotes that file addition is failed because the file already exists in the catalogue and it
	 * is read-only file.
	 */
	FILE_ADD_ERROR_EXIST_READ_ONLY,

	/** Denotes that file addition is failed due to an unknown error. */
	FILE_ADD_ERROR,

	/**
	 * Denotes that file addition is failed because access/permission denied or not authorized.
	 */
	FILE_ADD_ERROR_ACCESS_DENIED,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a master file to be deleted from the local storage(s).
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.
	 * <br/>The reply tag name is {@link #FILE_DELETE_MASTER_RESULT}.
	 */
	FILE_DELETE_MASTER,

	/**
	 * Sends the result of deleting a master file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of FILE_DELETE_XXXX where XXXX means the error/success
	 * message
	 */
	FILE_DELETE_MASTER_RESULT,

	/**
	 * Requests for a replica file to be deleted from the local storage(s).
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.
	 * <br/>The reply tag name is {@link #FILE_DELETE_REPLICA_RESULT}.
	 */
	FILE_DELETE_REPLICA,

	/**
	 * Sends the result of deleting a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of FILE_DELETE_XXXX where XXXX means the error/success
	 * message
	 */
	FILE_DELETE_REPLICA_RESULT,

	/** Denotes that file deletion is successful. */
	FILE_DELETE_SUCCESSFUL,

	/** Denotes that file deletion is failed due to an unknown error. */
	FILE_DELETE_ERROR,

	/** Denotes that file deletion is failed because it is a read-only file. */
	FILE_DELETE_ERROR_READ_ONLY,

	/**
	 * Denotes that file deletion is failed because the file does not exist in the storage nor
	 * catalogue.
	 */
	FILE_DELETE_ERROR_DOESNT_EXIST,

	/**
	 * Denotes that file deletion is failed because it is currently used by others.
	 */
	FILE_DELETE_ERROR_IN_USE,

	/**
	 * Denotes that file deletion is failed because access/permission denied or not authorized.
	 */
	FILE_DELETE_ERROR_ACCESS_DENIED,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Requests for a file to be modified from the local storage(s).
	 * <br/>The format of this request is Object[2] = {File obj, Integer senderID}.
	 * <br/>The reply tag name is {@link #FILE_MODIFY_RESULT}.
	 */
	FILE_MODIFY,

	/**
	 * Sends the result of deleting a file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of FILE_MODIFY_XXXX where XXXX means the error/success
	 * message
	 */
	FILE_MODIFY_RESULT,

	/** Denotes that file modification is successful. */
	FILE_MODIFY_SUCCESSFUL,

	/** Denotes that file modification is failed due to an unknown error. */
	FILE_MODIFY_ERROR,

	/**
	 * Denotes that file modification is failed because it is a read-only file.
	 */
	FILE_MODIFY_ERROR_READ_ONLY,

	/**
	 * Denotes that file modification is failed because the file does not exist.
	 */
	FILE_MODIFY_ERROR_DOESNT_EXIST,

	/**
	 * Denotes that file modification is failed because the file is currently used by others.
	 */
	FILE_MODIFY_ERROR_IN_USE,

	/**
	 * Denotes that file modification is failed because access/permission denied or not authorized.
	 */
	FILE_MODIFY_ERROR_ACCESS_DENIED,

	// ////////// REPLICA CATALOGUE TAGS

	// ***********************User<-->RC******************************//

	/**
	 * Denotes the request for a location of a replica file.
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.
	 * <br/>The reply tag name is {@link #CTLG_REPLICA_DELIVERY}.
	 * <br/>NOTE: This request only ask for one location only not all.
	 */
	CTLG_GET_REPLICA,

	/**
	 * Sends the result for a location of a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resourceID}.
	 * <br/>NOTE: The resourceID could be <tt>-1</tt> if not found.
	 */
	CTLG_REPLICA_DELIVERY,

	/**
	 * Denotes the request for all locations of a replica file.
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.
	 * <br/>The reply tag name is {@link #CTLG_REPLICA_LIST_DELIVERY}.
	 */
	CTLG_GET_REPLICA_LIST,

	/**
	 * Sends the result for all locations of a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, List locationList}.
	 * <br/>NOTE: The locationList could be <tt>null</tt> if not found.
	 */
	CTLG_REPLICA_LIST_DELIVERY,

	/**
	 * Denotes the request to get the attribute of a file.
	 * <br/>The format of this request is Object[2] = {String lfn, Integer senderID}.
	 * <br/>The reply tag name is {@link #CTLG_FILE_ATTR_DELIVERY}.
	 */
	CTLG_GET_FILE_ATTR,

	/**
	 * Sends the result for a file attribute back to sender.
	 * <br/>The format of the reply is {FileAttribute fileAttr}
	 * <br/>NOTE: The fileAttr could be <tt>null</tt> if not found.
	 */
	CTLG_FILE_ATTR_DELIVERY,

	/**
	 * Denotes the request to get a list of file attributes based on the given filter.
	 * <br/>The format of this request is Object[2] = {Filter filter, Integer senderID}
	 * <br/>The reply tag name is {@link #CTLG_FILTER_DELIVERY}.
	 */
	CTLG_FILTER,

	/**
	 * Sends the result for a list of file attributes back to sender.
	 * <br/>The format of the reply is {List attrList}.
	 * <br/>NOTE: The attrList could be <tt>null</tt> if not found.
	 */
	CTLG_FILTER_DELIVERY,

	// ***********************RM<-->RC******************************//

	/**
	 * Denotes the request to register / add a master file to the Replica Catalogue.
	 * <br/>The format of this request is Object[3] = {String filename, FileAttribute attr, Integer
	 * resID}.
	 * <br/>The reply tag name is {@link #CTLG_ADD_MASTER_RESULT}.
	 */
	CTLG_ADD_MASTER,

	/**
	 * Sends the result of registering a master file back to sender.
	 * <br/>The format of the reply is Object[3] = {String filename, Integer uniqueID, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of CTLG_ADD_MASTER_XXXX where XXXX means the error/success
	 * message
	 */
	CTLG_ADD_MASTER_RESULT,

	/** Denotes that master file addition is successful. */
	CTLG_ADD_MASTER_SUCCESSFUL,

	/** Denotes that master file addition is failed due to an unknown error. */
	CTLG_ADD_MASTER_ERROR,

	/**
	 * Denotes that master file addition is failed due to the catalogue is full.
	 */
	CTLG_ADD_MASTER_ERROR_FULL,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to de-register / delete a master file from the Replica Catalogue.
	 * <br/>The format of this request is Object[2] = {String lfn, Integer resourceID}.
	 * <br/>The reply tag name is {@link #CTLG_DELETE_MASTER_RESULT}.
	 */
	CTLG_DELETE_MASTER,

	/**
	 * Sends the result of de-registering a master file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of CTLG_DELETE_MASTER_XXXX where XXXX means the
	 * error/success message
	 */
	CTLG_DELETE_MASTER_RESULT,

	/** Denotes that master file deletion is successful. */
	CTLG_DELETE_MASTER_SUCCESSFUL,

	/** Denotes that master file deletion is failed due to an unknown error. */
	CTLG_DELETE_MASTER_ERROR,

	/**
	 * Denotes that master file deletion is failed because the file does not exist in the catalogue.
	 */
	CTLG_DELETE_MASTER_DOESNT_EXIST,

	/**
	 * Denotes that master file deletion is failed because replica files are still in the catalogue.
	 * All replicas need to be deleted first.
	 */
	CTLG_DELETE_MASTER_REPLICAS_EXIST,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to register / add a replica file to the Replica Catalogue.
	 * <br/>The format of this request is Object[2] = {String lfn, Integer resourceID}.
	 * <br/>The reply tag name is {@link #CTLG_ADD_REPLICA_RESULT}.
	 */
	CTLG_ADD_REPLICA,

	/**
	 * Sends the result of registering a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of CTLG_ADD_REPLICA_XXXX where XXXX means the
	 * error/success message
	 */
	CTLG_ADD_REPLICA_RESULT,

	/** Denotes that replica file addition is successful. */
	CTLG_ADD_REPLICA_SUCCESSFUL,

	/** Denotes that replica file addition is failed due to an unknown error. */
	CTLG_ADD_REPLICA_ERROR,

	/**
	 * Denotes that replica file addition is failed because the given file name does not exist in
	 * the catalogue.
	 */
	CTLG_ADD_REPLICA_ERROR_DOESNT_EXIST,

	/**
	 * Denotes that replica file addition is failed due to the catalogue is full.
	 */
	CTLG_ADD_REPLICA_ERROR_FULL,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to de-register / delete a replica file from the Replica Catalogue.<br/>
	 * The format of this request is Object[2] = {String lfn, Integer resourceID}.<br/>
	 * The reply tag name is {@link #CTLG_DELETE_REPLICA_RESULT}.
	 */
	CTLG_DELETE_REPLICA,

	/**
	 * Sends the result of de-registering a replica file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of CTLG_DELETE_REPLICA_XXXX where XXXX means the
	 * error/success message
	 */
	CTLG_DELETE_REPLICA_RESULT,

	/** Denotes that replica file deletion is successful. */
	CTLG_DELETE_REPLICA_SUCCESSFUL,

	/** Denotes that replica file deletion is failed due to an unknown error. */
	CTLG_DELETE_REPLICA_ERROR,

	/**
	 * Denotes that replica file deletion is failed because the file does not exist in the catalogue.
	 */
	CTLG_DELETE_REPLICA_ERROR_DOESNT_EXIST,

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Denotes the request to modify an existing master file information stored in the Replica
	 * Catalogue.
	 * <br/>The format of this request is Object[3] = {String filename, FileAttribute attr, Integer
	 * resID}.
	 * <br/>The reply tag name is {@link #CTLG_MODIFY_MASTER_RESULT}.
	 */
	CTLG_MODIFY_MASTER,

	/**
	 * Sends the result of modifying a master file back to sender.
	 * <br/>The format of the reply is Object[2] = {String lfn, Integer resultID}.
	 * <br/>NOTE: The result id is in the form of CTLG_MODIFY_MASTER_XXXX where XXXX means the
	 * error/success message
	 */
	CTLG_MODIFY_MASTER_RESULT,

	/** Denotes that master file deletion is successful. */
	CTLG_MODIFY_MASTER_SUCCESSFUL,

	/**
	 * Denotes that master file modification is failed due to an unknown error.
	 */
	CTLG_MODIFY_MASTER_ERROR,

	/**
	 * Denotes that master file modification is failed because the file does not exist in the
	 * catalogue.
	 */
	CTLG_MODIFY_MASTER_ERROR_DOESNT_EXIST,

	/**
	 * Denotes that master file modification is failed because the file attribute is set to a
	 * read-only.
	 */
	CTLG_MODIFY_MASTER_ERROR_READ_ONLY
}
