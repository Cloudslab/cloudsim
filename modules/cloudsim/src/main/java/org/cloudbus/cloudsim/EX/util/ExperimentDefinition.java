package org.cloudbus.cloudsim.EX.util;

/**
 * 
 * A definition/description of an experiment.
 * 
 * @author nikolay.grozev
 * 
 */
public class ExperimentDefinition {

    public static int GIGABYTE_IN_MEGA = 1024;

    private final Class<?> mainClass;
    private final int maxMem;
    private final int minMem;
    private final String[] arguements;

    /**
     * Constr.
     * 
     * @param mainClass
     *            - the main class of the experiment
     * @param maxMem
     *            - maximum heap size in megabytes. If -1 - the default max heap
     *            size is used.
     * @param minMem
     *            - minimum heap size in megabytes. If -1 - the default max heap
     *            size is used.
     * @param arguements
     *            - JVM and program arguements.
     */
    public ExperimentDefinition(Class<?> mainClass, int maxMem, int minMem, String... arguements) {
        super();
        this.mainClass = mainClass;
        this.maxMem = maxMem;
        this.minMem = minMem;
        this.arguements = arguements;
    }

    public Class<?> getMainClass() {
        return mainClass;
    }

    public int getMaxMem() {
        return maxMem;
    }

    public int getMinMem() {
        return minMem;
    }

    public String[] getArguements() {
        return arguements;
    }
}
