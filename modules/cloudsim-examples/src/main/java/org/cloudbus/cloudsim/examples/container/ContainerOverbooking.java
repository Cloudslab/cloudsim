package org.cloudbus.cloudsim.examples.container;


import org.apache.commons.cli.*;
import org.cloudbus.cloudsim.Log;

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
    private Options options = null;
    private String[] args = null;
    private RunConfig rc = new RunConfig();

    public RunConfig getRunConfig() {
        return rc;
    }

    public void setRc(RunConfig rc) {
        this.rc = rc;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }


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
//    private String containerAllocationPolicy = "Auction";

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
    }

    private void parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        RunConfig rc = this.getRunConfig();

        try {
            cmd = parser.parse(options, this.args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("runtime")) {
                this.rc.setRunTime(Integer.parseInt(cmd.getOptionValue("runtime")));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("i")) {
                this.rc.setRepeat(Integer.parseInt(cmd.getOptionValue("i")));;
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("container")) {
                this.rc.setContainerType(cmd.getOptionValue("container"));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("s")) {
                this.rc.setNumberHosts(Integer.parseInt(cmd.getOptionValue("s")));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("vms")) {
                this.rc.setNumberVms(Integer.parseInt(cmd.getOptionValue("vms")));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("c")) {
                this.rc.setNumberCloudlets(Integer.parseInt(cmd.getOptionValue("c")));
                // Whatever you want to do with the setting goes here
            }
            if (cmd.hasOption("cap")) {
                this.rc.setContainerAllocationPolicy(cmd.getOptionValue("cap"));
                // Whatever you want to do with the setting goes here
            }

        } catch (ParseException e) {
            Log.printLine("Failed to parse comand line properties " + e);
            help();
        }
//        System.out.println(rc);
        Log.printLine("Configuration: " + rc);
    }

    /*
     *
     */
    private void buildOptions() {

        options = new Options();

        options.addOption(Option.builder("r")
                .longOpt("runtime")
                .required(false)
                .hasArg()
                .type(Number.class)
                .desc("run Time")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("iteration")
                .required(false)
                .hasArg()
                .type(Number.class)
                .desc("iterations")
                .build());
        options.addOption(Option.builder("c")
                .longOpt("container")
                .required(false)
                .hasArg()
                .desc("Type of Container")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("hosts")
                .required(false)
                .hasArg()
                .type(Number.class)
                .desc("Number of Hosts")
                .build());
        options.addOption(Option.builder("v")
                .longOpt("vms")
                .required(false)
                .hasArg()
                .type(Number.class)
                .desc("Number of VMs")
                .build());
        options.addOption(Option.builder("c")
                .longOpt("cloudlets")
                .required(false)
                .hasArg()
                .type(Number.class)
                .desc("Number of Cloudlets")
                .build());
        options.addOption(Option.builder("p")
                .longOpt("cap")
                .required(false)
                .hasArg()
                .desc("Container Allocation Policy")
                .build());


    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        // create Options object

        ContainerOverbooking co = new ContainerOverbooking();
        co.setArgs(args);

        /**
         * The experiments can be repeated for (repeat - runtime +1) times.
         * Please set these values as the arguments of the main function or set them bellow:
         */
        co.buildOptions();
        co.parse();
        RunConfig rc = co.getRunConfig();
        rc.setInputFolder(ContainerOverbooking.class.getClassLoader().getResource("workload/planetlab").getPath());
        rc.setOutputFolder(Paths.get(".").toAbsolutePath().normalize().toString() + "/Results");

        for (int i = 10; i < rc.getRepeat(); i += 10) {
//            RunnerInitiator ri = new RunnerInitiator(rc, i);
            RunnerInitiator ri = new RunnerInitiator();
            ri.setRc(rc);
            ri.initializeAndStart(i);

//            new RunnerInitiator(
//                    enableOutput,
//                    outputToFile,
//                    inputFolder,
//                    outputFolder,
//                    vmAllocationPolicy,
//                    co.getContainerAllocationPolicy(),
//                    vmSelectionPolicy,
//                    containerSelectionPolicy,
//                    hostSelectionPolicy,
//                    i, Integer.toString(co.getRunTime()), outputFolder);
        }

    }
}
