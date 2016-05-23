package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

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

    /*
     * (non-Javadoc)
     *
     * @see RunnerAbs
     */
    @Override
    protected void init(String inputFolder, double overBookingFactor) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);
//            setOverBookingFactor(overBookingFactor);
            broker = HelperEx.createBroker(overBookingFactor);
            int brokerId = broker.getId();
            cloudletList = HelperEx.createContainerCloudletList(brokerId, inputFolder, ConstantsExamples.NUMBER_CLOUDLETS);
            containerList = HelperEx.createContainerList(brokerId, ConstantsExamples.NUMBER_CLOUDLETS);
            vmList = HelperEx.createVmList(brokerId, ConstantsExamples.NUMBER_VMS);
            hostList = HelperEx.createHostList(ConstantsExamples.NUMBER_HOSTS);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }
    }


}
