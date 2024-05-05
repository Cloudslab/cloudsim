/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;

/**
 * AppCloudlet class represents an application which user submit for execution within a datacenter. It
 * consist of several {@link NetworkCloudlet NetworkCloudlets}.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 *
 * 
 * //TODO The attributes have to be defined as private.
 */
public class AppCloudlet extends Cloudlet {

	public int type;

	public int appID;

        /**
         * The list of {@link NetworkCloudlet} that this AppCloudlet represents.
         */
	public ArrayList<NetworkCloudlet> cList;

        /**
         * This attribute doesn't appear to be used.
         * Only the TestBagofTaskApp class is using it
         * and such a class appears to be used only for not
         * documented test (it is not a unit test).
         */
	public double deadline;

	public static final int APP_MC = 1;

	public static final int APP_Workflow = 3;

	public AppCloudlet(int type, int appID, double deadline, int userId) {
		// Access these parameters from within cList, NOT the appCloudlet object
		super(appID, -1, -1, -1, -1, null, null, null);
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		setUserId(userId);
		cList = new ArrayList<>();
	}

	/**
	 * An example of creating APPcloudlet
	 * 
	 * @param vmIdList VMs where Cloudlet will be executed
	 */
	/*public void createCloudletList(List<Integer> vmIdList) {
		for (int i = 0; i < numbervm; i++) {
			long length = 4;
			long fileSize = 300;
			long outputSize = 300;
			long memory = 256;
			int pesNumber = 4;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			// HPCCloudlet cl=new HPCCloudlet();
			NetworkCloudlet cl = new NetworkCloudlet(
					NetworkConstants.currentCloudletId,
					length,
					pesNumber,
					fileSize,
					outputSize,
					memory,
					utilizationModel,
					utilizationModel,
					utilizationModel);
			// setting the owner of these Cloudlets
			NetworkConstants.currentCloudletId++;
			cl.setUserId(getUserId());
			cl.submittime = CloudSim.clock();
			cl.currStagenum = -1;
			cList.add(cl);

		}
		// based on type

	}*/
}
