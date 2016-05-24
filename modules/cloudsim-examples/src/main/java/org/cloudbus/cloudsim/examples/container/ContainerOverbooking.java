package org.cloudbus.cloudsim.examples.container;


import java.io.IOException;

/**
 * This Example is following the format for {@link org.cloudbus.cloudsim.examples.power.planetlab.Dvfs}
 * It specifically studies the placement of containers.
 *
 * @author Sareh Fotuhi Piraghaj
 */
public class ContainerOverbooking {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        /**
         * The experiments can be repeated for (repeat - runtime +1) times.
         * Please set these values as the arguments of the main function or set them bellow:
         */
        int runTime = Integer.parseInt(args[0]);
        int repeat = Integer.parseInt(args[1]);
        for (int i = 10; i < repeat; i += 10) {
            boolean enableOutput = true;
            boolean outputToFile = true;
            /**
             * Getting the path of the planet lab workload that is included in the cloudSim Package
             */
            String inputFolder = ContainerOverbooking.class.getClassLoader().getResource("workload/planetlab").getPath();
            /**
             * The output folder for the logs. The log files would be located in this folder.
             */
            String outputFolder = "/Results";
            /**
             * The allocation policy for VMs. It has the under utilization and over utilization thresholds used for
             * determining the underloaded and oberloaded hosts.
             */
            String vmAllocationPolicy = "MSThreshold-Under_0.80_0.70"; // DVFS policy without VM migrations
            /**
             * The selection policy for containers where a container migration is triggered.
             */
            String containerSelectionPolicy = "Cor";
            /**
             * The allocation policy used for allocating containers to VMs.
             */
            String containerAllocationPolicy = "MostFull";
//            String containerAllocationPolicy= "FirstFit";
//            String containerAllocationPolicy= "LeastFull";
//            String containerAllocationPolicy= "Simple";
//            String containerAllocationPolicy = "Random";

            /**
             * The host selection policy determines which hosts should be selected as the migration destination.
             */
            String hostSelectionPolicy = "FirstFit";
            /**
             * The VM Selection Policy is used for selecting VMs to migrate when a host status is determined as
             * "Overloaded"
             */
            String vmSelectionPolicy = "VmMaxC";

            new RunnerInitiator(
                    enableOutput,
                    outputToFile,
                    inputFolder,
                    outputFolder,
                    vmAllocationPolicy,
                    containerAllocationPolicy,
                    vmSelectionPolicy,
                    containerSelectionPolicy,
                    hostSelectionPolicy,
                    i, Integer.toString(runTime), outputFolder);
        }

    }
}
