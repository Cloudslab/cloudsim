package org.cloudbus.cloudsim.examples.container;

import java.nio.file.Paths;

/**
 * Created by henri.vandenbulk on 3/20/17.
 */
public class RunConfig {

    private String containerType = ConstantsExamples.CONTAINER_VM_TYPE;
    private int numberHosts = ConstantsExamples.NUMBER_HOSTS;
    private int numberVms = ConstantsExamples.NUMBER_VMS;
    private int numberCloudlets = ConstantsExamples.NUMBER_CLOUDLETS;

    private boolean enableOutput = true;
    private boolean outputToFile = true;

    private String inputFolder;
    private String outputFolder;
    private String vmAllocationPolicy = "MSThreshold-Under_0.80_0.70"; // DVFS policy without VM migrations
    private String containerSelectionPolicy = "Cor";
    private String containerAllocationPolicy = "Simple";
    private String hostSelectionPolicy = "FirstFit";
    private String vmSelectionPolicy = "VmMaxC";
//    private double overBookingFactor

    private int runTime = 1;
    private int repeat = 100;

    public String getContainerAllocationPolicy() {
        return this.containerAllocationPolicy;
    }

    public void setContainerAllocationPolicy(String containerAllocationPolicy) {
        this.containerAllocationPolicy = containerAllocationPolicy;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public int getNumberHosts() {
        return numberHosts;
    }

    public void setNumberHosts(int numberHosts) {
        this.numberHosts = numberHosts;
    }

    public int getNumberVms() {
        return numberVms;
    }

    public void setNumberVms(int numberVms) {
        this.numberVms = numberVms;
    }

    public int getNumberCloudlets() {
        return numberCloudlets;
    }

    public void setNumberCloudlets(int numberCloudlets) {
        this.numberCloudlets = numberCloudlets;
    }

    public boolean isEnableOutput() {
        return enableOutput;
    }

    public void setEnableOutput(boolean enableOutput) {
        this.enableOutput = enableOutput;
    }

    public boolean isOutputToFile() {
        return outputToFile;
    }

    public void setOutputToFile(boolean outputToFile) {
        this.outputToFile = outputToFile;
    }

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getVmAllocationPolicy() {
        return vmAllocationPolicy;
    }

    public void setVmAllocationPolicy(String vmAllocationPolicy) {
        this.vmAllocationPolicy = vmAllocationPolicy;
    }

    public String getContainerSelectionPolicy() {
        return containerSelectionPolicy;
    }

    public void setContainerSelectionPolicy(String containerSelectionPolicy) {
        this.containerSelectionPolicy = containerSelectionPolicy;
    }

    public String getHostSelectionPolicy() {
        return hostSelectionPolicy;
    }

    public void setHostSelectionPolicy(String hostSelectionPolicy) {
        this.hostSelectionPolicy = hostSelectionPolicy;
    }

    public String getVmSelectionPolicy() {
        return vmSelectionPolicy;
    }

    public void setVmSelectionPolicy(String vmSelectionPolicy) {
        this.vmSelectionPolicy = vmSelectionPolicy;
    }

    public int getRunTime() {
        return this.runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        return "RunConfig{" +
                "containerType='" + containerType + '\'' +
                ", numberHosts=" + numberHosts +
                ", numberVms=" + numberVms +
                ", numberCloudlets=" + numberCloudlets +
                ", enableOutput=" + enableOutput +
                ", outputToFile=" + outputToFile +
                ", inputFolder='" + inputFolder + '\'' +
                ", outputFolder='" + outputFolder + '\'' +
                ", vmAllocationPolicy='" + vmAllocationPolicy + '\'' +
                ", containerSelectionPolicy='" + containerSelectionPolicy + '\'' +
                ", containerAllocationPolicy='" + containerAllocationPolicy + '\'' +
                ", hostSelectionPolicy='" + hostSelectionPolicy + '\'' +
                ", vmSelectionPolicy='" + vmSelectionPolicy + '\'' +
                ", runTime=" + runTime +
                ", repeat=" + repeat + '}';
    }

}
