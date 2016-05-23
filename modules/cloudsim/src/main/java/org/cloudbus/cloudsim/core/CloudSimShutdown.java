/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

/**
 * CloudimShutdown waits for termination of all CloudSim user entities to determine the end of
 * simulation. This class will be created by CloudSim upon initialisation of the simulation, i.e.
 * done via <tt>CloudSim.init()</tt> method. Hence, do not need to worry about creating an object of
 * this class. This object signals the end of simulation to CloudInformationService (CIS) entity.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class CloudSimShutdown extends SimEntity {

	/** The total number of cloud users. */
	private int numUser;

	/**
	 * Instantiates a new CloudSimShutdown object.
	 * <p/>
	 * The total number of cloud user entities plays an important role to determine whether all
	 * hostList' should be shut down or not. If one or more users are still not finished, then the
	 * hostList's will not be shut down. Therefore, it is important to give a correct number of total
	 * cloud user entities. Otherwise, CloudSim program will hang or encounter a weird behaviour.
	 * 
	 * @param name the name to be associated with this entity (as required by {@link SimEntity} class)
	 * @param numUser total number of cloud user entities
	 * @throws Exception when creating this entity before initialising CloudSim package
	 *             or this entity name is <tt>null</tt> or empty
	 * @see CloudSim#init(int, java.util.Calendar, boolean) 
	 * @pre name != null
	 * @pre numUser >= 0
	 * @post $none
         * 
         * @todo The use of Exception is not recommended. Specific exceptions
         * would be thrown (such as {@link IllegalArgumentException})
         * or {@link RuntimeException}
	 */
	public CloudSimShutdown(String name, int numUser) throws Exception {
		// NOTE: This entity doesn't use any I/O port.
		// super(name, CloudSimTags.DEFAULT_BAUD_RATE);
		super(name);
		this.numUser = numUser;
	}

	/**
	 * The main method that shuts down hostList's and Cloud Information Service (CIS). In addition,
	 * this method writes down a report at the end of a simulation based on
	 * <tt>reportWriterName</tt> defined in the Constructor. <br/>
	 * <b>NOTE:</b> This method shuts down cloud hostList's and CIS entities either <tt>AFTER</tt> all
	 * cloud users have been shut down or an entity requires an abrupt end of the whole simulation.
	 * In the first case, the number of cloud users given in the Constructor <tt>must</tt> be
	 * correct. Otherwise, CloudSim package hangs forever or it does not terminate properly.
	 * 
	 * @param ev the ev
	 * @pre $none
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		numUser--;
		if (numUser == 0 || ev.getTag() == CloudSimTags.ABRUPT_END_OF_SIMULATION) {
			CloudSim.abruptallyTerminate();
		}
	}

        /**
         * The method has no effect at the current class.
         */
	@Override
	public void startEntity() {
		// do nothing
	}
        
        /**
         * The method has no effect at the current class.
         */
	@Override
	public void shutdownEntity() {
		// do nothing
	}
}
