package org.cloudbus.cloudsim.examples.container;


import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Optional;
import java.nio.file.Paths;

/**
 * This Example is following the format for {@link org.cloudbus.cloudsim.examples.power.planetlab.Dvfs}
 * It specifically studies the placement of containers.
 *
 * @author Sareh Fotuhi Piraghaj
 */
public class ContainerOverbooking {
    private Options options = new Options();
    private String[] args = null;
    private int runTime = 1;
    private int repeat = 100;
    private String containerType = ConstantsExamples.CONTAINER_VM_TYPE;
    private int hosts = ConstantsExamples.NUMBER_HOSTS;
    private int vms = ConstantsExamples.NUMBER_VMS;
    private int cloudlets = ConstantsExamples.NUMBER_CLOUDLETS;

    /**
     * The allocation policy used for allocating containers to VMs.
     */
//            String containerAllocationPolicy = "MostFull";
//            String containerAllocationPolicy= "FirstFit";
//            String containerAllocationPolicy= "LeastFull";
//            String containerAllocationPolicy= "Simple";
//            String containerAllocationPolicy = "RandomSelection";
//            String containerAllocationPolicy = "Balanced";
//            String containerAllocationPolicy = "LeastRequested";
    private String containerAllocationPolicy = "Auction";

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
    }

    private void parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, this.args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("runtime")) {
                this.runTime = Integer.getInteger(cmd.getOptionValue("runtime"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("i")) {
                this.repeat = Integer.getInteger(cmd.getOptionValue("i"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("container")) {
                this.containerType = cmd.getOptionValue("container");
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("h")) {
                this.hosts = Integer.getInteger(cmd.getOptionValue("h"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("vm")) {
                this.vms = Integer.getInteger(cmd.getOptionValue("vm"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("c")) {
                this.cloudlets = Integer.getInteger(cmd.getOptionValue("c"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("cap")) {
                this.containerAllocationPolicy = cmd.getOptionValue("cap");
                // Whatever you want to do with the setting goes here
            }

        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse comand line properties", e);
            help();
        }
    }

    private void buildOptions() {


        options.addOption("runtime", false, "run Time")
                .addOption("i", false, "iterations")
                .addOption("container", false, "Type of container")
                .addOption("h", false, "Number of hosts")
                .addOption("vm", false, "Number of VMs")
                .addOption("c", false, "Number of Cloudlets")
                .addOptopn("cap", false, "Container Allocation Policy");
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        // create Options object
        this.args = args;

        /**
         * The experiments can be repeated for (repeat - runtime +1) times.
         * Please set these values as the arguments of the main function or set them bellow:
         */
        buildOptions();
        parse();


//        if (args.length > 2)  {
//            runTime = Integer.parseInt(args[0]);
//            repeat = Integer.parseInt(args[1]);
//        }
        for (int i = 10; i < this.repeat; i += 10) {
            boolean enableOutput = true;
            boolean outputToFile = true;
            /**
             * Getting the path of the planet lab workload that is included in the cloudSim Package
             */
            String inputFolder = ContainerOverbooking.class.getClassLoader().getResource("workload/planetlab").getPath();
            /**
             * The output folder for the logs. The log files would be located in this folder.
             */
            String outputFolder = Paths.get(".").toAbsolutePath().normalize().toString() + "/Results";
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
                    this.containerSelectionPolicy,
                    hostSelectionPolicy,
                    i, Integer.toString(this.runTime), outputFolder);
        }

    }
}
