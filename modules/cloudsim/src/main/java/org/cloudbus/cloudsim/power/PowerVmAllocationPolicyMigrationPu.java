// Pu Lr
package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;
import java.util.Set;
import jdk.nashorn.internal.runtime.PropertyMap;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 *
 * @author sabir
 */
public class PowerVmAllocationPolicyMigrationPu extends PowerVmAllocationPolicyMigrationAbstract{
    /** The scheduling interval. */
	private double schedulingInterval;

	/** The safety parameter. */
	private double safetyParameter;

	/** The fallback vm allocation policy. */
	private PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy;

	/**
	 * Instantiates a new power vm allocation policy migration local regression.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationPu(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy,
			double utilizationThreshold) {
		super(hostList, vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setSchedulingInterval(schedulingInterval);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}

	/**
	 * Instantiates a new power vm allocation policy migration local regression.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 */
	public PowerVmAllocationPolicyMigrationPu(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
		super(hostList, vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setSchedulingInterval(schedulingInterval);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}
        
	/**
	 * Checks if is host over utilized.
	 * 
	 * @param host the host
	 * @return true, if is host over utilized
	 */
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double[] utilizationHistory = _host.getUtilizationHistory();
		int length = 10; // we use 10 to make the regression responsive enough to latest values
		if (utilizationHistory.length < length) {
			return getFallbackVmAllocationPolicy().isHostOverUtilized(host);
		}
		double[] utilizationHistoryReversed = new double[length];
		for (int i = 0; i < length; i++) {
			utilizationHistoryReversed[i] = utilizationHistory[length - i - 1];
		}
		double[] estimates = null;
		try {
			estimates = getParameterEstimates(utilizationHistoryReversed);
		} catch (IllegalArgumentException e) {
			return getFallbackVmAllocationPolicy().isHostOverUtilized(host);
		}
		double migrationIntervals = Math.ceil(getMaximumVmMigrationTime(_host) / getSchedulingInterval());
		double predictedUtilization = estimates[0] + estimates[1] * (length + migrationIntervals);
		predictedUtilization *= getSafetyParameter();

		addHistoryEntry(host, predictedUtilization);

		return predictedUtilization >= 1;
	}
        
        public double getPredictedUtilization (PowerHost host,Vm vm)throws IllegalStateException{
            PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double[] utilizationHistory = _host.getUtilizationHistory();
		int length = 10; // we use 10 to make the regression responsive enough to latest values
		if (utilizationHistory.length < length) {
			return getMaxUtilizationAfterAllocation(host, vm);
                       //throw new IllegalStateException("There is not enough Host history to estimate its utilization using Local Regression");
		}
		double[] utilizationHistoryReversed = new double[length];
		for (int i = 0; i < length; i++) {
			utilizationHistoryReversed[i] = utilizationHistory[length - i - 1];
		}
		double[] estimates = null;
		try {
			estimates = getParameterEstimates(utilizationHistoryReversed);
		} catch (IllegalArgumentException e) {
			return getMaxUtilizationAfterAllocation(host, vm);
		}
		double migrationIntervals = Math.ceil(getMaximumVmMigrationTime(_host) / getSchedulingInterval());
		double predictedUtilization = estimates[0] + estimates[1] * (length + migrationIntervals);
		predictedUtilization *= getSafetyParameter();
		return predictedUtilization;
        }
        
    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
         PowerHost allocatedHost = null;
         double predictedUtilization = Double.MAX_VALUE;
        for(PowerHost host : this.<PowerHost> getHostList()){
            if (excludedHosts.contains(host)) {
                continue;
            }
            if (host.isSuitableForVm(vm)) {
                if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                    continue;
		}
            
                
                double pu = getPredictedUtilization(host,vm);
                if(pu<predictedUtilization){
                    predictedUtilization = pu;
                    allocatedHost = host;
                }else continue;
                //allocatedHost = host;
            }
        }
        return allocatedHost;
    }

   @Override
    protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {
        double maxP2Nratio = Double.MIN_VALUE;
        double minUtilization = 1;
        PowerHost underloadedHost = null;
        for(PowerHost host: this.<PowerHost> getHostList()){
            if(excludedHosts.contains(host)){
                continue;
            }
            double utilization = host.getUtilizationOfCpu();
            if (utilization > 0 && utilization < minUtilization
					&& !areAllVmsMigratingOutOrAnyVmMigratingIn(host)){
                double power = host.getPower();
                int num_VMs = host.getVmList().size();
                double P2Nratio = power/(double)num_VMs;
                minUtilization = utilization;
                if(P2Nratio>maxP2Nratio){
                    underloadedHost = host;
                    maxP2Nratio = P2Nratio;
            }
        }
        }
        return underloadedHost;
    }
            

	/**
	 * Gets the parameter estimates.
	 * 
	 * @param utilizationHistoryReversed the utilization history reversed
	 * @return the parameter estimates
	 */
	protected double[] getParameterEstimates(double[] utilizationHistoryReversed) {
		return MathUtil.getLoessParameterEstimates(utilizationHistoryReversed);
	}

	/**
	 * Gets the maximum vm migration time.
	 * 
	 * @param host the host
	 * @return the maximum vm migration time
	 */
	protected double getMaximumVmMigrationTime(PowerHost host) {
		int maxRam = Integer.MIN_VALUE;
		for (Vm vm : host.getVmList()) {
			int ram = vm.getRam();
			if (ram > maxRam) {
				maxRam = ram;
			}
		}
		return maxRam / ((double) host.getBw() / (2 * 8000));
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the fallback vm allocation policy.
	 * 
	 * @param fallbackVmAllocationPolicy the new fallback vm allocation policy
	 */
	public void setFallbackVmAllocationPolicy(
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
		this.fallbackVmAllocationPolicy = fallbackVmAllocationPolicy;
	}

	/**
	 * Gets the fallback vm allocation policy.
	 * 
	 * @return the fallback vm allocation policy
	 */
	public PowerVmAllocationPolicyMigrationAbstract getFallbackVmAllocationPolicy() {
		return fallbackVmAllocationPolicy;
	}

	public double getSafetyParameter() {
		return safetyParameter;
	}

	public void setSafetyParameter(double safetyParameter) {
		this.safetyParameter = safetyParameter;
	}
}
