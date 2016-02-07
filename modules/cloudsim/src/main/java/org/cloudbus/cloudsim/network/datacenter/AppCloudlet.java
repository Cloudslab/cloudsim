/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

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
 * @since CloudSim Toolkit 1.0
 * 
 * @todo If it is an application/cloudlet, it would extend the Cloudlet class.
 * In the case of Cloudlet class has more attributes and methods than
 * required by this class, a common interface would be created.
 * 
 * @todo The attributes have to be defined as private.
 */
public class AppCloudlet {

	public int type;

	public int appID;

        /**
         * The list of {@link NetworkCloudlet} that this AppCloudlet represents.
         */
	public ArrayList<NetworkCloudlet> clist;

        /**
         * This attribute doesn't appear to be used.
         * Only the TestBagofTaskApp class is using it
         * and such a class appears to be used only for not
         * documented test (it is not a unit test).
         */
	public double deadline;

        /**
         * This attribute doesn't appear to be used.
         */
	public double accuracy;

        /**
         * Number of VMs the AppCloudlet can use.
         * @todo the attribute would be renamed to numberOfVMs or something 
         * like that.
         */
	public int numbervm;

        /**
         * Id of the AppCloudlet's owner.
         */
	public int userId;

        /**
         * @todo It would be "execTime". This attribute is very strange.
         * The the todo in the TestBagofTaskApp class.
         */
	public double exeTime;

        /**
         * This attribute doesn't appear to be used.
         */
	public int requestclass;

        public static final int APP_MC = 1;

	public static final int APP_Workflow = 3;

	public AppCloudlet(int type, int appID, double deadline, int numbervm, int userId) {
		super();
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		this.numbervm = numbervm;
		this.userId = userId;
		clist = new ArrayList<NetworkCloudlet>();
	}

	/**
	 * An example of creating APPcloudlet
	 * 
	 * @param vmIdList VMs where Cloudlet will be executed
         * @todo This method is very strange too. It creates the internal cloudlet list
         * with cloudlets of hard-coded defined attributes, such as
         * fileSize, outputSize and length, what doesn't make sense.
         * If this class is to be an example, it should be 
         * inside the example package. As an example, it make senses the
         * hard-coded values.
	 */
	public void createCloudletList(List<Integer> vmIdList) {
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
			cl.setUserId(userId);
			cl.submittime = CloudSim.clock();
			cl.currStagenum = -1;
			clist.add(cl);

		}
		// based on type

	}
}
