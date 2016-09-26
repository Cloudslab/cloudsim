package org.cloudbus.cloudsim.googletrace;

public class GoogleTask implements Comparable<GoogleTask>{

    private int id, priority;
    private double submitTime, runtime, cpuReq, memReq, startTime, finishTime;

    public GoogleTask(int id, double submitTime, double runTime,
                      double cpuReq, double memReq, int priority) {
        this.id = id;
        this.submitTime = submitTime;
        this.runtime = runTime;
        this.cpuReq = cpuReq;
        this.memReq = memReq;
        this.priority = priority;
    }

    @Override
    public int compareTo(GoogleTask other) {
        if (getSubmitTime() < other.getSubmitTime()) {
            return -1;
        } else if (getSubmitTime() > other.getSubmitTime()) {
            return 1;
        } else if (getPriority() < other.getPriority()) {
            return -1;
        } else if (getPriority() > other.getPriority()) {
            return 1;
        }
        return new Integer(getId()).compareTo(new Integer(other.getId()));
    }


    public int getId() {
        return id;
    }

    public double getSubmitTime() {
        return submitTime;
    }

    public double getRuntime() {
        return runtime;
    }

    public double getCpuReq() {
        return cpuReq;
    }

    public double getMemReq() {
        return memReq;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoogleTask that = (GoogleTask) o;

        if (id != that.id) return false;
        if (priority != that.priority) return false;
        if (Double.compare(that.submitTime, submitTime) != 0) return false;
        if (Double.compare(that.runtime, runtime) != 0) return false;
        if (Double.compare(that.cpuReq, cpuReq) != 0) return false;
        if (Double.compare(that.memReq, memReq) != 0) return false;
        if (Double.compare(that.startTime, startTime) != 0) return false;
        return Double.compare(that.finishTime, finishTime) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + priority;
        temp = Double.doubleToLongBits(submitTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(runtime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cpuReq);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(memReq);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(startTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(finishTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
