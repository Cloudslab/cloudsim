package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;

import java.util.Calendar;

/**
 * This is the modified version of {@link org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabRunner} in CloudSim Package.
 * Created by sareh on 18/08/15.
 */

public class RunnerInitiator extends RunnerAbs {

    /**
     * Instantiates a new runner.
     *
     * @param enableOutput       the enable output
     * @param outputToFile       the output to file
     * @param inputFolder        the input folder
     * @param outputFolder       the output folder
     *                           //     * @param workload the workload
     * @param vmAllocationPolicy the vm allocation policy
     * @param vmSelectionPolicy  the vm selection policy
     */
    public RunnerInitiator(
            boolean enableOutput,
            boolean outputToFile,
            String inputFolder,
            String outputFolder,
            String vmAllocationPolicy,
            String containerAllocationPolicy,
            String vmSelectionPolicy,
            String containerSelectionPolicy,
            String hostSelectionPolicy,
            double overBookingFactor, String runTime, String logAddress) {

        super(enableOutput,
                outputToFile,
                inputFolder,
                outputFolder,
                vmAllocationPolicy,
                containerAllocationPolicy,
                vmSelectionPolicy,
                containerSelectionPolicy,
                hostSelectionPolicy,
                overBookingFactor, runTime, logAddress);

    }

    public RunnerInitiator() {

    }

//    public RunnerInitiator(
//            RunConfig rc,
//            double overBookingFactor) {
//
//        this.rc = rc;
//
//        initializeAndStart(
//                rc.isEnableOutput(),
//                rc.isOutputToFile(),
//                rc.getInputFolder(),
//                rc.getOutputFolder(),
//                rc.getVmAllocationPolicy(),
//                rc.getContainerAllocationPolicy(),
//                rc.getVmSelectionPolicy(),
//                rc.getContainerSelectionPolicy(),
//                rc.getHostSelectionPolicy(),
//                overBookingFactor, Integer.toString(rc.getRunTime()), rc.getOutputFolder()
//        );
//
//
//
//    }

    /*
     * (non-Javadoc)
     *
     * @see RunnerAbs
     */
    @Override
    protected void init(String inputFolder, double overBookingFactor) {
        try {
            Log.printLine("Initializing Simulation");

            CloudSim.init(1, Calendar.getInstance(), false);
//            setOverBookingFactor(overBookingFactor);
            broker = HelperEx.createBroker(overBookingFactor);

//            broker = HelperEx.createAuctionBroker(overBookingFactor);
            int brokerId = broker.getId();
//            cloudletList = HelperEx.createContainerCloudletList(brokerId, inputFolder, ConstantsExamples.NUMBER_CLOUDLETS);
            cloudletList = HelperEx.createContainerCloudletList(brokerId, inputFolder, rc.getNumberCloudlets());
//            containerList = HelperEx.createContainerList(brokerId, ConstantsExamples.NUMBER_CLOUDLETS);
            containerList = HelperEx.createContainerList(brokerId, rc.getNumberCloudlets());
//            vmList = HelperEx.createVmList(brokerId, ConstantsExamples.NUMBER_VMS, ConstantsExamples.CONTAINER_VM_TYPE);
            vmList = HelperEx.createVmList(brokerId, ConstantsExamples.NUMBER_VMS, rc.getContainerType());
//            hostList = HelperEx.createHostList(ConstantsExamples.NUMBER_HOSTS);
            hostList = HelperEx.createHostList(rc.getNumberHosts());

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }
    }


}
