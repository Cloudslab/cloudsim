/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.network.datacenter.TaskStage;

/**
 * Network constants
 * //@TODO This class uses several hard-coded values that appears to be used
 * only for examples. If yes, it should be moved to the examples package.
 * The exceptions are the  {@link TaskStage} types and number of
 * switches by level.
 */
public class NetworkConstants {

	public static int maxhostVM = 2;
	public static int HOST_PEs = 8;

	public static double maxMemperVM = 1024 * 1024;// kb

	public static int currentCloudletId = 0;
	public static int currentAppId = 0;


	public static final int PES_NUMBER = 4;
	public static final int FILE_SIZE = 300;
	public static final int OUTPUT_SIZE = 300;

	public static final int COMMUNICATION_LENGTH = 1;


	public static long BandWidthEdgeAgg = 100 * 1024 * 1024;// 100 Megabits
	public static long BandWidthEdgeHost = 100 * 1024 * 1024;//
	public static long BandWidthAggRoot = 20 * 1024 * 1024 * 2;// 40gb


	public static double SwitchingDelayRoot = .00285; // ms
	public static double SwitchingDelayAgg  = .00245; // ms
	public static double SwitchingDelayEdge = .00157; // ms

	public static double EdgeSwitchPort = 4;// number of host
	public static double AggSwitchPort = 1;// number of Edge
	public static double RootSwitchPort = 1;// number of Agg

	public static double seed = 199;

	public static boolean logflag = false;

	public static int nexttime = 1000;
}
