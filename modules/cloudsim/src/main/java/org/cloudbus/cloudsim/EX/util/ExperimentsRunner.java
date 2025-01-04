package org.cloudbus.cloudsim.EX.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * A utility that runs a set of experiments in different JVM processes. Since
 * CloudSim makes heavy use of static data, experiments can not be run in
 * multiple threads within the same JVM. With this utility class one can spawn
 * multiple independent JVM process, redirect their standard outputs to a single
 * place and synchronize with their ends.
 * 
 * <br>
 * <br>
 * Each experiment is specified by a class with a main method and an output
 * file, where the output from the CustomLog is stored. Each class's main method
 * should take two parameters - the output file and the config file for the
 * logger. It is responsibility of the implementers of these classes to parse
 * and use these parameters.
 * 
 * @author nikolay.grozev
 * 
 */
public class ExperimentsRunner {

    private static final List<Process> PROCESSES = Collections.synchronizedList(new ArrayList<>());
    private static Thread shutdownHook = null;

    /**
     * Runs a set of experiments in separated processes. If only one experiment
     * is provided - it is run in the current process. Allows users to specify
     * how many processors to remain idle, even if they are needed for the
     * faster execution of the experiment. It is wise to leave at least 1 CPU
     * idle if you're running experiments on your PC or laptop, so that it does
     * not freeze.
     * 
     * @param experimentsDefs
     *            - the experiments' definitions.
     * @param numFreeCPUs
     *            - number of processors to leave unused. Must be non-negative
     *            and less than the number processors - 1. For example if 0 -
     *            all processors/cores can be used if required. If 1 and the
     *            system has multiple CPUs - then 1 CPU will be free at all
     *            times.
     * 
     * @throws Exception
     *             - if something goes wrong.
     */
    public static synchronized void runExperiments(final List<ExperimentDefinition> experimentsDefs,
            final int numFreeCPUs) throws Exception {

        if (!experimentsDefs.isEmpty()) {
            // Prints the pid of the current process... so we know who to kill
            printPIDInformation();

            // If this process dies - kill the spawn subprocesses.
            addHookToKillProcesses();

            // If possible leave the requested processors free
            int cores = Runtime.getRuntime().availableProcessors();
            int coresToUse = cores <= numFreeCPUs ? 1 : cores - numFreeCPUs;

            ExecutorService pool = Executors.newFixedThreadPool(coresToUse);
            Collection<Future<?>> futures = new ArrayList<>();

            for (final ExperimentDefinition def : experimentsDefs) {
                Runnable runnable = () -> {
                    int resultStatus;
                    try {
                        resultStatus = exec(def);
                    } catch (IOException | InterruptedException e) {
                        resultStatus = 1;
                    }
                    if (resultStatus != 0) {
                        System.err.println("!!! Experiment " + def.getMainClass().getCanonicalName()
                                + " has failed!!!");
                    }
                };
                futures.add(pool.submit(runnable));
            }

            // Wait until all are finished
            for (Future<?> future : futures) {
                future.get();
            }

            pool.shutdown();
        }
        System.err.println();
        System.err.println("All experiments are finished");
    }

    private static int[] getHeapArgs() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        int minMem = 64;
        int maxMem = 2;
        for (String arg : arguments) {
            char lastChar = arg.charAt(arg.length());
            if (arg.startsWith("-Xmx")) {
                maxMem = Integer.parseInt(arg.trim().replaceAll("[^\\d]", ""));
                maxMem = normaliseToMegabytes(maxMem, lastChar);
            } else if (arg.startsWith("-Xms")) {
                minMem = Integer.parseInt(arg.trim().replaceAll("[^\\d]", ""));
                minMem = normaliseToMegabytes(minMem, lastChar);
            }
        }
        return new int[] { maxMem, minMem };
    }

    private static int normaliseToMegabytes(int mem, char lastChar) {
        int res = mem;
        if (lastChar == 'g' || lastChar == 'G') {
            res = mem * 1024;
        } else if (lastChar == 'k' || lastChar == 'K') {
            res = mem / 1024;
        } else if (lastChar != 'm' || lastChar == 'M') {
            res = mem / 1048576;
        }
        return res;
    }

    private synchronized static void addHookToKillProcesses() {
        if (shutdownHook == null) {
            shutdownHook = new Thread(() -> {
                System.err.println("Killing subprocesses...");
                for (Process p : PROCESSES) {
                    p.destroy();
                }
                System.err.println("All subprocesses are killed. Shutting down.");
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    /**
     * Inspired by <a href=
     * "http://stackoverflow.com/questions/636367/executing-a-java-application-in-a-separate-process"
     * >StackOverflow: Executing java in a separate process</a>
     * 
     * @param def
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static int exec(final ExperimentDefinition def) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = def.getMainClass().getCanonicalName();

        List<String> vmParams = new ArrayList<>();
        List<String> appParams = new ArrayList<>();

        if (def.getMaxMem() > 0) {
            vmParams.add("-Xmx" + def.getMaxMem() + "m");
        }
        if (def.getMinMem() > 0) {
            vmParams.add("-Xms" + def.getMinMem() + "m");
        }

        for (String param : def.getArguements()) {
            if (param.startsWith("-X") || param.startsWith("-D")) {
                vmParams.add(param);
            } else {
                appParams.add(param);
            }
        }

        List<String> processBuilderList = new ArrayList<>();
        processBuilderList.add(javaBin);
        processBuilderList.addAll(vmParams);
        processBuilderList.addAll(Arrays.asList("-cp", classpath, className));
        processBuilderList.addAll(appParams);
        ProcessBuilder builder = new ProcessBuilder(processBuilderList.toArray(new String[0]));

        // Redirect the standard I/O to here (this process)
        builder.inheritIO();

        // Start the process
        Process process = builder.start();

        // Keep a reference to the process, so that it can be killed
        PROCESSES.add(process);

        // Wait until the process is done.
        process.waitFor();

        // Return the status of the process
        return process.exitValue();
    }

    private static void printPIDInformation() throws IOException {
        if (SystemUtils.IS_OS_LINUX) {
            byte[] bo = new byte[100];
            String[] cmd = { "bash", "-c", "echo $PPID" };
            Process p = Runtime.getRuntime().exec(cmd);
            p.getInputStream().read(bo);

            String pid = new String(bo).trim();
            System.err.println("Main process Id (PID) is: " + pid + ". Use: ");
            System.err.println("\tkill -SIGINT " + pid);
            System.err.println("to kill all experiments");
            System.err.println();
        } else {
            // @TODO implement for other OS-es or in a platform independent way
            System.err.println("Could not detect the PID of the current processess ...");
        }
    }
}
